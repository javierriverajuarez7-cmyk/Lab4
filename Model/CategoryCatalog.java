package Model;

import java.util.List;

public final class CategoryCatalog {
    private static final List<ProductCategory> CATEGORIES = List.of(
        new ProductCategory("Café en pergamino", List.of("Humedad del grano (%)"),
            (code, producer, date, kg, quality, v) -> new ParchmentCoffee(code, producer, date, kg, quality, Input.toDouble(v.get(0), "humedad"))),
        new ProductCategory("Café orgánico", List.of("Humedad del grano (%)", "Entidad certificadora", "Sello válido hasta (AAAA-MM-DD)"),
            (code, producer, date, kg, quality, v) -> new OrganicCoffee(code, producer, date, kg, quality, Input.toDouble(v.get(0), "humedad"), v.get(1), Input.toDate(v.get(2), "fecha de validez del sello"))),
        new ProductCategory("Cardamomo", List.of("Grano verde (%)"),
            (code, producer, date, kg, quality, v) -> new Cardamom(code, producer, date, kg, quality, Input.toDouble(v.get(0), "grano verde"))),
        new ProductCategory("Hortaliza", List.of("Vida útil (días, 3 a 14)"),
            (code, producer, date, kg, quality, v) -> new Vegetable(code, producer, date, kg, quality, Input.toInt(v.get(0), "vida útil"))),
        new ProductCategory("Queso fresco", List.of("Grasa (%)"),
            (code, producer, date, kg, quality, v) -> new FreshCheese(code, producer, date, kg, quality, Input.toDouble(v.get(0), "grasa")))
    );

    private CategoryCatalog(){}

    public static List<ProductCategory> getAll(){
        return CATEGORIES;
    }
}
