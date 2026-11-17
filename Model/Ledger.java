package Model;

import java.util.Map;
import java.util.TreeMap;

public class Ledger {
    private final Map<String, Double> paid = new TreeMap<>();
    private final Map<String, Double> charged = new TreeMap<>();
    private final Map<String, Double> losses = new TreeMap<>();

    public void addPaid(String category, double amount){paid.merge(category, amount, Double::sum);}

    public void addCharged(String category, double amount){charged.merge(category, amount, Double::sum);}

    public void addLoss(String category, double amount){losses.merge(category, amount, Double::sum);}

    public double getPaid(String category){return paid.getOrDefault(category, 0.0);}

    public double getCharged(String category){return charged.getOrDefault(category, 0.0);}

    public double getLosses(String category){return losses.getOrDefault(category, 0.0);}
}
