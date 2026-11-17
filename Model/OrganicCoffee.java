package Model;

import java.time.LocalDate;

public class OrganicCoffee extends ParchmentCoffee {
    private static final double BONUS_PER_KG = 0.75;
    private final String certifier;
    private final LocalDate sealValidUntil;

    public OrganicCoffee(String code, String producer, LocalDate entryDate, double receivedKg, Quality quality, double humidity, String certifier, LocalDate sealValidUntil){
        super(code, producer, entryDate, receivedKg, quality, humidity);
        if(certifier == null || certifier.isBlank()){
            throw new InvalidOperationException("La entidad certificadora no puede estar vacía.");
        }
        if(sealValidUntil == null || sealValidUntil.isBefore(entryDate)){
            throw new InvalidOperationException("El sello de certificación está vencido en la fecha de ingreso.");
        }
        this.certifier = certifier.trim();
        this.sealValidUntil = sealValidUntil;
    }

    public String getCertifier(){return certifier;}

    public LocalDate getSealValidUntil(){return sealValidUntil;}

    @Override
    public String getCategory(){
        return "Café orgánico";
    }

    @Override
    public double calculatePayment(){
        return round(super.calculatePayment() + getReceivedKg() * BONUS_PER_KG);
    }
}
