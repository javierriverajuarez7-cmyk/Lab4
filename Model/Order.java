package Model;

public record Order(String category, double kg) {

    public Order {
        if(category == null || category.isBlank()){
            throw new InvalidOperationException("La categoría del pedido no puede estar vacía.");
        }
        if(!(kg > 0) || Double.isInfinite(kg)){
            throw new InvalidOperationException("Los kilogramos del pedido deben ser positivos.");
        }
    }
}
