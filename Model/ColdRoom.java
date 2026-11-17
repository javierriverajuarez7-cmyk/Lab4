package Model;

public class ColdRoom extends Warehouse {

    public ColdRoom(String code, double capacityKg){
        super(code, capacityKg);
    }

    @Override
    protected boolean isCompatible(Lot lot){
        return lot.asColdStorageRequired().isPresent();
    }

    @Override
    public String getType(){
        return "cámara fría";
    }
}
