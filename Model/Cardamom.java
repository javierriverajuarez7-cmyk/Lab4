package Model;

import java.time.LocalDate;
import java.util.Optional;

public class Cardamom extends Lot implements Perishable {
    private static final int SHELF_LIFE_DAYS = 180;
    private final double greenGrain;

    public Cardamom(String code, String producer, LocalDate entryDate, double receivedKg, Quality quality, double greenGrain){
        super(code, producer, entryDate, receivedKg, quality);
        if(!(greenGrain >= 0 && greenGrain <= 100)){
            throw new InvalidOperationException("El grano verde debe estar entre 0 y 100 %.");
        }
        this.greenGrain = greenGrain;
    }

    private static double rate(Quality quality){
        return switch(quality){
            case A -> 60;
            case B -> 48;
            case C -> 35;
        };
    }

    public double getGreenGrain(){return greenGrain;}

    @Override
    public String getCategory(){
        return "Cardamomo";
    }

    @Override
    public double calculatePayment(){
        double payment = getReceivedKg() * rate(getQuality());
        if(greenGrain >= 85){
            payment *= 1.10;
        }
        return round(payment);
    }

    @Override
    public int getShelfLifeDays(){
        return SHELF_LIFE_DAYS;
    }

    @Override
    public Optional<Perishable> asPerishable(){
        return Optional.of(this);
    }
}
