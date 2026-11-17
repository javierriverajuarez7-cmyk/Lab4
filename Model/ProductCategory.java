package Model;

import java.time.LocalDate;
import java.util.List;

public class ProductCategory {
    private final String name;
    private final List<String> fields;
    private final LotCreator creator;

    public ProductCategory(String name, List<String> fields, LotCreator creator){
        this.name = name;
        this.fields = List.copyOf(fields);
        this.creator = creator;
    }

    public String getName(){return name;}

    public List<String> getFields(){return fields;}

    public Lot createLot(String code, String producer, LocalDate entryDate, double kg, Quality quality, List<String> values){
        return creator.create(code, producer, entryDate, kg, quality, values);
    }
}
