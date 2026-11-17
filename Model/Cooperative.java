package Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Cooperative {
    public static final double SURCHARGE = 0.35;

    public record PreSettlement(String category, double payment, String expiration) {}

    public record DispatchLine(String lotCode, String warehouseCode, double kg, double amount) {}

    public record Dispatch(Order order, List<DispatchLine> lines, double total) {}

    public record Discard(String lotCode, String category, double kg, double loss) {}

    private LocalDate operationDate;
    private final Map<String, Lot> lots = new LinkedHashMap<>();
    private final Map<String, Warehouse> warehouses = new LinkedHashMap<>();
    private final Ledger ledger = new Ledger();

    public Cooperative(LocalDate operationDate){
        if(operationDate == null){
            throw new InvalidOperationException("La fecha inicial es obligatoria.");
        }
        this.operationDate = operationDate;
    }

    private static String key(String code){
        return code.trim().toUpperCase();
    }

    public void registerWarehouse(Warehouse warehouse){
        if(warehouses.containsKey(key(warehouse.getCode()))){
            throw new InvalidOperationException("Ya existe una bodega con el código " + warehouse.getCode() + ".");
        }
        warehouses.put(key(warehouse.getCode()), warehouse);
    }

    private Warehouse findWarehouse(String code){
        if(code == null || code.isBlank()){
            throw new InvalidOperationException("El código de la bodega no puede estar vacío.");
        }
        Warehouse warehouse = warehouses.get(key(code));
        if(warehouse == null){
            throw new InvalidOperationException("No existe la bodega " + code.trim() + ".");
        }
        return warehouse;
    }

    private void checkNewCode(Lot lot){
        if(lots.containsKey(key(lot.getCode()))){
            throw new InvalidOperationException("Ya existe un lote con el código " + lot.getCode() + ".");
        }
    }

    public PreSettlement preSettle(Lot lot){
        checkNewCode(lot);
        return new PreSettlement(lot.getCategory(), lot.calculatePayment(), lot.getExpirationText());
    }

    public double receive(Lot lot, String warehouseCode){
        checkNewCode(lot);
        if(!lot.getEntryDate().equals(operationDate)){
            throw new InvalidOperationException("Todo lote nuevo ingresa en la fecha de operación (" + operationDate + ").");
        }
        Warehouse warehouse = findWarehouse(warehouseCode);
        warehouse.store(lot);
        lots.put(key(lot.getCode()), lot);
        double payment = lot.calculatePayment();
        ledger.addPaid(lot.getCategory(), payment);
        return payment;
    }

    public void loadInitial(Lot lot, String warehouseCode){
        checkNewCode(lot);
        if(lot.getEntryDate().isAfter(operationDate)){
            throw new InvalidOperationException("La fecha de ingreso no puede ser posterior a la fecha de operación.");
        }
        findWarehouse(warehouseCode).store(lot);
        lots.put(key(lot.getCode()), lot);
    }

    public List<Discard> advanceDays(int days){
        if(days < 0){
            throw new InvalidOperationException("La fecha de operación no puede retroceder.");
        }
        if(days == 0){
            throw new InvalidOperationException("Debe avanzar al menos un día.");
        }
        operationDate = operationDate.plusDays(days);
        return discardExpired();
    }

    private List<Discard> discardExpired(){
        List<Discard> discards = new ArrayList<>();
        for(Warehouse warehouse : warehouses.values()){
            Iterator<Lot> it = warehouse.iterator();
            while(it.hasNext()){
                Lot lot = it.next();
                if(lot.hasExpired(operationDate)){
                    double kg = lot.getAvailableKg();
                    double loss = Math.round(lot.discard() * 100.0) / 100.0;
                    it.remove();
                    ledger.addLoss(lot.getCategory(), loss);
                    discards.add(new Discard(lot.getCode(), lot.getCategory(), kg, loss));
                }
            }
        }
        return discards;
    }

    public Dispatch dispatch(Order order){
        List<Lot> candidates = new ArrayList<>();
        for(Lot l : lotsInExitOrder()){
            if(l.getCategory().equalsIgnoreCase(order.category())){
                candidates.add(l);
            }
        }
        if(candidates.isEmpty()){
            throw new InvalidOperationException("No hay existencias de " + order.category() + ".");
        }
        double available = 0;
        for(Lot l : candidates){
            available += l.getAvailableKg();
        }
        if(available + 1e-9 < order.kg()){
            throw new InvalidOperationException(String.format(Locale.US, "El pedido no puede cubrirse: pide %.2f kg y hay %.2f kg.", order.kg(), available));
        }
        double remaining = order.kg();
        double total = 0;
        List<DispatchLine> lines = new ArrayList<>();
        for(Lot lot : candidates){
            if(remaining <= 1e-9){
                break;
            }
            double taken = Math.min(remaining, lot.getAvailableKg());
            double amount = Math.round(taken * lot.getCostPerKg() * (1 + SURCHARGE) * 100.0) / 100.0;
            Warehouse warehouse = lot.getWarehouse();
            lot.dispatch(taken);
            if(lot.getState() == LotState.DEPLETED){
                warehouse.remove(lot);
            }
            lines.add(new DispatchLine(lot.getCode(), warehouse.getCode(), taken, amount));
            total += amount;
            remaining -= taken;
        }
        ledger.addCharged(candidates.get(0).getCategory(), total);
        return new Dispatch(order, lines, total);
    }

    public LocalDate getOperationDate(){return operationDate;}

    public Ledger getLedger(){return ledger;}

    public Collection<Warehouse> getWarehouses(){
        return Collections.unmodifiableCollection(warehouses.values());
    }

    public List<Lot> lotsInExitOrder(){
        List<Lot> all = new ArrayList<>();
        for(Warehouse w : warehouses.values()){
            for(Lot l : w){
                all.add(l);
            }
        }
        Collections.sort(all);
        return all;
    }

    public List<Lot> lotsOutOfWarehouse(){
        List<Lot> out = new ArrayList<>();
        for(Lot l : lots.values()){
            if(l.getState() != LotState.AVAILABLE){
                out.add(l);
            }
        }
        return out;
    }

    public Map<String, List<Lot>> stockByCategory(){
        Map<String, List<Lot>> result = new LinkedHashMap<>();
        for(Lot l : lotsInExitOrder()){
            result.computeIfAbsent(l.getCategory(), k -> new ArrayList<>()).add(l);
        }
        return result;
    }

    public List<Lot> expiringSoon(int days){
        LocalDate limit = operationDate.plusDays(days);
        List<Lot> result = new ArrayList<>();
        for(Lot l : lotsInExitOrder()){
            l.asPerishable().ifPresent(p -> {
                LocalDate expiration = p.getExpirationDate();
                if(!expiration.isBefore(operationDate) && !expiration.isAfter(limit)){
                    result.add(l);
                }
            });
        }
        return result;
    }
}
