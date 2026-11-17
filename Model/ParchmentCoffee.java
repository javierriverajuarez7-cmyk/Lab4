package Model;

import java.time.LocalDate;

public class ParchmentCoffee extends Lot {
    private final double humidity;

    public ParchmentCoffee(String code, String producer, LocalDate entryDate, double receivedKg, Quality quality, double humidity){
        super(code, producer, entryDate, receivedKg, quality);
        if(!(humidity >= 8 && humidity <= 14)){
            throw new InvalidOperationException("La humedad debe estar entre 8 y 14 %.");
        }
        this.humidity = humidity;
    }

    private static double rate(Quality quality){
        return switch(quality){
            case A -> 12;
            case B -> 9;
            case C -> 6;
        };
    }

    public double getHumidity(){return humidity;}

    @Override
    public String getCategory(){
        return "Café en pergamino";
    }

    @Override
    public double calculatePayment(){
        double base = getReceivedKg() * rate(getQuality());
        int points = (int) Math.ceil(Math.max(0, humidity - 12) - EPS);
        return round(base - base * 0.03 * points);
    }
}
