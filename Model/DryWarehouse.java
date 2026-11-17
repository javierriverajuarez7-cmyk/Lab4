package Model;

public class DryWarehouse extends Warehouse {

    public DryWarehouse(String code, double capacityKg){
        super(code, capacityKg);
    }

    @Override
    protected boolean isCompatible(Lot lot){
        return lot.asColdStorageRequired().isEmpty();
    }

    @Override
    public String getType(){
        return "bodega seca";
    }
}
