package Model;

public interface ColdStorageRequired {
    default boolean requiresColdStorage(){
        return true;
    }
}
