package Model;

import java.time.LocalDate;
import java.util.Optional;

public class Vegetable extends Lot implements Perishable, ColdStorageRequired {
    private static final double PAID_FRACTION = 0.95;
    private final int shelfLifeDays;

    public Vegetable(String code, String producer, LocalDate entryDate, double receivedKg, Quality quality, int shelfLifeDays){
        super(code, producer, entryDate, receivedKg, quality);
        if(shelfLifeDays < 3 || shelfLifeDays > 14){
            throw new InvalidOperationException("La vida útil debe ser un entero entre 3 y 14 días.");
        }
        this.shelfLifeDays = shelfLifeDays;
    }

    private static double rate(Quality quality){
        return switch(quality){
            case A -> 4;
            case B -> 3;
            case C -> 2;
        };
    }

    @Override
    public String getCategory(){
        return "Hortaliza";
    }

    @Override
    public double calculatePayment(){
        return round(getReceivedKg() * PAID_FRACTION * rate(getQuality()));
    }

    @Override
    public int getShelfLifeDays(){
        return shelfLifeDays;
    }

    @Override
    public Optional<Perishable> asPerishable(){
        return Optional.of(this);
    }

    @Override
    public Optional<ColdStorageRequired> asColdStorageRequired(){
        return Optional.of(this);
    }
}
