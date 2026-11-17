package Model;

import java.util.Iterator;
import java.util.TreeSet;

public abstract class Warehouse implements Iterable<Lot> {
    private final String code;
    private final double capacityKg;
    private final TreeSet<Lot> lots = new TreeSet<>();

    protected Warehouse(String code, double capacityKg){
        if(code == null || code.isBlank()){
            throw new InvalidOperationException("El código de la bodega no puede estar vacío.");
        }
        if(!(capacityKg > 0) || Double.isInfinite(capacityKg)){
            throw new InvalidOperationException("La capacidad debe ser un número positivo.");
        }
        this.code = code.trim();
        this.capacityKg = capacityKg;
    }

    /** @return true si la bodega admite el lote */
    protected abstract boolean isCompatible(Lot lot);

    public abstract String getType();

    public void validate(Lot lot){
        if(!isCompatible(lot)){
            throw new InvalidOperationException("El lote " + lot.getCode() + " (" + lot.getCategory() + ") no es compatible con la bodega " + code + " (" + getType() + ").");
        }
        if(getUsedKg() + lot.getReceivedKg() > capacityKg + 1e-9){
            throw new InvalidOperationException("El lote excede la capacidad de la bodega " + code + " (libre: " + String.format("%.2f", getFreeKg()) + " kg).");
        }
    }

    public void store(Lot lot){
        validate(lot);
        lot.assignWarehouse(this);
        lots.add(lot);
    }

    public void remove(Lot lot){
        lots.remove(lot);
    }

    public double getUsedKg(){
        double sum = 0;
        for(Lot l : lots){
            sum += l.getAvailableKg();
        }
        return sum;
    }

    public double getFreeKg(){return capacityKg - getUsedKg();}

    public String getCode(){return code;}

    public double getCapacityKg(){return capacityKg;}

    @Override
    public Iterator<Lot> iterator(){
        return lots.iterator();
    }
}
