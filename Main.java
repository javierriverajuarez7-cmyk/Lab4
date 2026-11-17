import Model.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.BiFunction;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static final Cooperative cooperative = new Cooperative(LocalDate.of(2026, 11, 15));

    public static void main(String[] args){
        try {
            loadInitialData();
        } catch(InvalidOperationException e){
            System.out.println("Error en los datos iniciales: " + e.getMessage());
            return;
        }
        System.out.println("=== AcopioGT ===");
        boolean exit = false;
        while(!exit){
            showMenu();
            try {
                switch(read("Opción").trim()){
                    case "1" -> preSettle();
                    case "2" -> receiveLot();
                    case "3" -> registerWarehouse();
                    case "4" -> orderAndDispatch();
                    case "5" -> advanceDate();
                    case "6" -> consult();
                    case "7" -> reports();
                    case "0" -> exit = true;
                    default -> System.out.println("Opción no válida.");
                }
            } catch(InvalidOperationException e){
                System.out.println("✗ Operación rechazada: " + e.getMessage() + " (no se modificó nada)");
            } catch(NoSuchElementException e){
                System.out.println("\nFin de la entrada. Saliendo.");
                exit = true;
            }
        }
        System.out.println("Hasta pronto.");
    }

    private static void loadInitialData(){
        cooperative.registerWarehouse(new DryWarehouse("BS-01", 2000));
        cooperative.registerWarehouse(new DryWarehouse("BS-02", 800));
        cooperative.registerWarehouse(new ColdRoom("CF-01", 500));
        cooperative.loadInitial(new ParchmentCoffee("CP-001", "Juan Pérez", LocalDate.of(2026, 11, 1), 500, Quality.A, 13.4), "BS-01");
        cooperative.loadInitial(new ParchmentCoffee("CP-002", "María López", LocalDate.of(2026, 11, 1), 300, Quality.B, 10.0), "BS-02");
        cooperative.loadInitial(new OrganicCoffee("CO-001", "Coop. Cobán", LocalDate.of(2026, 10, 20), 200, Quality.A, 11.0, "OrgCert GT", LocalDate.of(2027, 6, 30)), "BS-01");
        cooperative.loadInitial(new OrganicCoffee("CO-002", "Rosa Tzoc", LocalDate.of(2026, 11, 5), 150, Quality.B, 12.5, "OrgCert GT", LocalDate.of(2027, 3, 31)), "BS-01");
        cooperative.loadInitial(new Cardamom("CA-001", "Pedro Caal", LocalDate.of(2026, 11, 3), 80, Quality.B, 90), "BS-01");
        cooperative.loadInitial(new Cardamom("CA-002", "Ana Xol", LocalDate.of(2026, 10, 10), 120, Quality.A, 80), "BS-02");
        cooperative.loadInitial(new Vegetable("HO-001", "Luis Ajú", LocalDate.of(2026, 11, 10), 200, Quality.A, 7), "CF-01");
        cooperative.loadInitial(new Vegetable("HO-002", "Carmen Ixcoy", LocalDate.of(2026, 11, 12), 150, Quality.B, 10), "CF-01");
        cooperative.loadInitial(new FreshCheese("QU-001", "Lácteos Tecpán", LocalDate.of(2026, 11, 5), 40, Quality.A, 25), "CF-01");
        cooperative.loadInitial(new FreshCheese("QU-002", "Lácteos Tecpán", LocalDate.of(2026, 11, 8), 60, Quality.B, 30), "CF-01");
    }

    private static void showMenu(){
        System.out.println("\n--- Menú (fecha de operación: " + cooperative.getOperationDate() + ") ---");
        System.out.println("1. Pre-liquidar lote");
        System.out.println("2. Recibir lote");
        System.out.println("3. Registrar bodega");
        System.out.println("4. Registrar pedido y despachar");
        System.out.println("5. Avanzar fecha de operación");
        System.out.println("6. Consultar lotes y bodegas");
        System.out.println("7. Reportes");
        System.out.println("0. Salir");
    }

    private static String read(String prompt){
        System.out.print(prompt + ": ");
        return in.nextLine();
    }

    private static ProductCategory chooseCategory(){
        List<ProductCategory> categories = CategoryCatalog.getAll();
        for(int i = 0; i < categories.size(); i++){
            System.out.println((i + 1) + ". " + categories.get(i).getName());
        }
        int option = Input.toInt(read("Categoría"), "categoría");
        if(option < 1 || option > categories.size()){
            throw new InvalidOperationException("Categoría fuera de rango.");
        }
        return categories.get(option - 1);
    }

    private static Lot readLot(){
        ProductCategory category = chooseCategory();
        String code = read("Código del lote");
        String producer = read("Nombre del productor");
        double kg = Input.toDouble(read("Peso recibido (kg)"), "peso");
        Quality quality = Input.toQuality(read("Calidad (A/B/C)"));
        List<String> values = new ArrayList<>();
        for(String field : category.getFields()){
            values.add(read(field));
        }
        return category.createLot(code, producer, cooperative.getOperationDate(), kg, quality, values);
    }

    private static void showPreSettlement(Cooperative.PreSettlement p){
        System.out.println("Pre-liquidación de " + p.category() + ": pago " + Format.money(p.payment()) + " | vencimiento: " + p.expiration());
    }

    private static void preSettle(){
        showPreSettlement(cooperative.preSettle(readLot()));
        System.out.println("(Pre-liquidación: no se registró nada.)");
    }

    private static void receiveLot(){
        Lot lot = readLot();
        showPreSettlement(cooperative.preSettle(lot));
        if(!read("¿El productor acepta? (s/n)").trim().equalsIgnoreCase("s")){
            System.out.println("No aceptada. No se registró nada.");
            return;
        }
        for(Warehouse w : cooperative.getWarehouses()){
            System.out.println("  " + w.getCode() + " (" + w.getType() + ") libres: " + Format.kg(w.getFreeKg()));
        }
        double payment = cooperative.receive(lot, read("Código de la bodega"));
        System.out.println("✓ Lote " + lot.getCode() + " recibido en " + lot.getWarehouse().getCode() + ". Pago registrado: " + Format.money(payment));
    }

    private static void registerWarehouse(){
        List<String> types = List.of("Bodega seca", "Cámara fría");
        List<BiFunction<String, Double, Warehouse>> factories = List.of(DryWarehouse::new, ColdRoom::new);
        for(int i = 0; i < types.size(); i++){
            System.out.println((i + 1) + ". " + types.get(i));
        }
        int option = Input.toInt(read("Tipo"), "tipo");
        if(option < 1 || option > types.size()){
            throw new InvalidOperationException("Tipo de bodega fuera de rango.");
        }
        String code = read("Código");
        double capacity = Input.toDouble(read("Capacidad (kg)"), "capacidad");
        Warehouse warehouse = factories.get(option - 1).apply(code, capacity);
        cooperative.registerWarehouse(warehouse);
        System.out.println("✓ Bodega " + warehouse.getCode() + " registrada (" + warehouse.getType() + ", " + Format.kg(warehouse.getCapacityKg()) + ").");
    }

    private static void orderAndDispatch(){
        ProductCategory category = chooseCategory();
        double kg = Input.toDouble(read("Kilogramos solicitados"), "kilogramos");
        Order order = new Order(category.getName(), kg);
        System.out.println("Pedido registrado: " + Format.kg(order.kg()) + " de " + order.category());
        Cooperative.Dispatch dispatch = cooperative.dispatch(order);
        for(Cooperative.DispatchLine line : dispatch.lines()){
            System.out.println("  " + line.lotCode() + " (bodega " + line.warehouseCode() + "): " + Format.kg(line.kg()) + " -> " + Format.money(line.amount()));
        }
        System.out.println("✓ Despacho completo. Total cobrado: " + Format.money(dispatch.total()));
    }

    private static void advanceDate(){
        int days = Input.toInt(read("Días a avanzar"), "días");
        List<Cooperative.Discard> discards = cooperative.advanceDays(days);
        System.out.println("✓ Nueva fecha de operación: " + cooperative.getOperationDate());
        if(discards.isEmpty()){
            System.out.println("No se descartó ningún lote.");
        }
        for(Cooperative.Discard d : discards){
            System.out.println("  Descartado " + d.lotCode() + " (" + d.category() + "): " + Format.kg(d.kg()) + " | pérdida " + Format.money(d.loss()));
        }
    }

    private static void consult(){
        System.out.println("\nLotes almacenados (orden de salida):");
        for(Lot l : cooperative.lotsInExitOrder()){
            System.out.println("  " + l);
        }
        List<Lot> out = cooperative.lotsOutOfWarehouse();
        if(!out.isEmpty()){
            System.out.println("Lotes fuera de bodega:");
            for(Lot l : out){
                System.out.println("  " + l);
            }
        }
        System.out.println("\nBodegas:");
        printWarehouses();
    }

    private static void printWarehouses(){
        for(Warehouse w : cooperative.getWarehouses()){
            System.out.println(w.getCode() + " [" + w.getType() + "] ocupado " + Format.kg(w.getUsedKg()) + " de " + Format.kg(w.getCapacityKg()));
            for(Lot l : w){
                System.out.println("    " + l);
            }
        }
    }

    private static void reports(){
        System.out.println("\n== Reporte 1: existencias por categoría ==");
        Map<String, List<Lot>> stock = cooperative.stockByCategory();
        for(ProductCategory c : CategoryCatalog.getAll()){
            List<Lot> lots = stock.getOrDefault(c.getName(), List.of());
            double total = 0;
            for(Lot l : lots){
                total += l.getAvailableKg();
            }
            System.out.println(c.getName() + " - " + Format.kg(total));
            for(Lot l : lots){
                System.out.println("    " + l.getCode() + ": " + Format.kg(l.getAvailableKg()));
            }
        }
        System.out.println("Contenido de cada bodega:");
        printWarehouses();
        System.out.println("\n== Reporte 2: perecederos que vencen en los próximos 3 días ==");
        List<Lot> soon = cooperative.expiringSoon(3);
        if(soon.isEmpty()){
            System.out.println("Ninguno.");
        }
        for(Lot l : soon){
            System.out.println("  " + l.getCode() + " " + l.getCategory() + " vence " + l.getExpirationText() + " (" + Format.kg(l.getAvailableKg()) + ")");
        }
        System.out.println("\n== Reporte 3: totales por categoría ==");
        Ledger ledger = cooperative.getLedger();
        System.out.printf("%-20s %14s %14s %14s%n", "Categoría", "Pagado", "Cobrado", "Pérdidas");
        for(ProductCategory c : CategoryCatalog.getAll()){
            System.out.printf("%-20s %14s %14s %14s%n", c.getName(), Format.money(ledger.getPaid(c.getName())), Format.money(ledger.getCharged(c.getName())), Format.money(ledger.getLosses(c.getName())));
        }
    }
}
