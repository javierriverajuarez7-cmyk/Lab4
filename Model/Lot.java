package Model;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

public abstract class Lot implements Comparable<Lot> {
    protected static final double EPS = 1e-9;
    private final String code;
    private final String producer;
    private final LocalDate entryDate;
    private final double receivedKg;
    private final Quality quality;
    private double availableKg;
    private LotState state;
    private Warehouse warehouse;

    protected Lot(String code, String producer, LocalDate entryDate, double receivedKg, Quality quality){
        if(code == null || code.isBlank()){
            throw new InvalidOperationException("El código del lote no puede estar vacío.");
        }
        if(producer == null || producer.isBlank()){
            throw new InvalidOperationException("El nombre del productor no puede estar vacío.");
        }
        if(entryDate == null){
            throw new InvalidOperationException("La fecha de ingreso es obligatoria.");
        }
        if(!(receivedKg > 0) || Double.isInfinite(receivedKg)){
            throw new InvalidOperationException("El peso debe ser un número positivo.");
        }
        if(quality == null){
            throw new InvalidOperationException("La calidad es obligatoria.");
        }
        this.code = code.trim();
        this.producer = producer.trim();
        this.entryDate = entryDate;
        this.receivedKg = receivedKg;
        this.quality = quality;
        this.availableKg = receivedKg;
        this.state = LotState.AVAILABLE;
    }

    /** @return nombre de la categoría */
    public abstract String getCategory();

    /** @return pago total al productor por el lote */
    public abstract double calculatePayment();

    public Optional<Perishable> asPerishable(){
        return Optional.empty();
    }

    public Optional<ColdStorageRequired> asColdStorageRequired(){
        return Optional.empty();
    }

    public boolean hasExpired(LocalDate today){
        return asPerishable().map(p -> p.isExpired(today)).orElse(false);
    }

    public String getExpirationText(){
        return asPerishable().map(p -> p.getExpirationDate().toString()).orElse("no vence");
    }

    public double getCostPerKg(){
        return calculatePayment() / receivedKg;
    }

    public void dispatch(double kg){
        if(state != LotState.AVAILABLE){
            throw new InvalidOperationException("El lote " + code + " no está disponible.");
        }
        if(!(kg > 0) || kg > availableKg + EPS){
            throw new InvalidOperationException("Cantidad inválida para el lote " + code + ".");
        }
        availableKg -= kg;
        if(availableKg <= EPS){
            availableKg = 0;
            state = LotState.DEPLETED;
        }
    }

    public double discard(){
        double loss = availableKg * getCostPerKg();
        availableKg = 0;
        state = LotState.DISCARDED;
        return loss;
    }

    void assignWarehouse(Warehouse warehouse){
        if(this.warehouse != null){
            throw new InvalidOperationException("El lote " + code + " ya tiene bodega asignada.");
        }
        this.warehouse = warehouse;
    }

    protected static double round(double value){
        return Math.round(value * 100.0) / 100.0;
    }

    public String getCode(){return code;}

    public String getProducer(){return producer;}

    public LocalDate getEntryDate(){return entryDate;}

    public double getReceivedKg(){return receivedKg;}

    public Quality getQuality(){return quality;}

    public double getAvailableKg(){return availableKg;}

    public LotState getState(){return state;}

    public Warehouse getWarehouse(){return warehouse;}

    @Override
    public int compareTo(Lot other){
        Optional<Perishable> a = asPerishable();
        Optional<Perishable> b = other.asPerishable();
        if(a.isPresent() != b.isPresent()){
            return a.isPresent() ? -1 : 1;
        }
        int result = a.isPresent()
                ? a.get().getExpirationDate().compareTo(b.get().getExpirationDate())
                : entryDate.compareTo(other.entryDate);
        if(result != 0){
            return result;
        }
        result = quality.compareTo(other.quality);
        if(result != 0){
            return result;
        }
        return code.compareTo(other.code);
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof Lot l && l.code.equals(code);
    }

    @Override
    public int hashCode(){
        return code.hashCode();
    }

    @Override
    public String toString(){
        return String.format(Locale.US, "%-7s %-18s Cal.%s %8.2f/%.2f kg  Ingreso %s  Vence: %-10s Estado: %s%s",
                code, getCategory(), quality, availableKg, receivedKg, entryDate, getExpirationText(), state,
                warehouse == null ? "" : "  Bodega: " + warehouse.getCode());
    }
}
