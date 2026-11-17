package Model;

import java.time.LocalDate;
import java.util.Optional;

public class FreshCheese extends Lot implements Perishable, ColdStorageRequired {
    private static final int SHELF_LIFE_DAYS = 21;
    private static final double PACKAGING_PER_KG = 1.50;
    private final double fat;

    public FreshCheese(String code, String producer, LocalDate entryDate, double receivedKg, Quality quality, double fat){
        super(code, producer, entryDate, receivedKg, quality);
        rate(quality);
        if(!(fat >= 10 && fat <= 40)){
            throw new InvalidOperationException("La grasa debe estar entre 10 y 40 %.");
        }
        this.fat = fat;
    }

    private static double rate(Quality quality){
        return switch(quality){
            case A -> 38;
            case B -> 30;
            case C -> throw new InvalidOperationException("No se acepta queso fresco de calidad C.");
        };
    }

    public double getFat(){return fat;}

    @Override
    public String getCategory(){
        return "Queso fresco";
    }

    @Override
    public double calculatePayment(){
        return round(getReceivedKg() * (rate(getQuality()) - PACKAGING_PER_KG));
    }

    @Override
    public int getShelfLifeDays(){
        return SHELF_LIFE_DAYS;
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
