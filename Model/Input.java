package Model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class Input {

    private Input(){}

    public static double toDouble(String text, String field){
        try {
            double value = Double.parseDouble(text.trim().replace(',', '.'));
            if(Double.isNaN(value) || Double.isInfinite(value)){
                throw new NumberFormatException();
            }
            return value;
        } catch(RuntimeException e){
            throw new InvalidOperationException("El valor de " + field + " no es un número válido: '" + text + "'.");
        }
    }

    public static int toInt(String text, String field){
        try {
            return Integer.parseInt(text.trim());
        } catch(RuntimeException e){
            throw new InvalidOperationException("El valor de " + field + " no es un entero válido: '" + text + "'.");
        }
    }

    public static LocalDate toDate(String text, String field){
        try {
            return LocalDate.parse(text.trim());
        } catch(DateTimeParseException e){
            throw new InvalidOperationException("La " + field + " debe tener formato AAAA-MM-DD: '" + text + "'.");
        }
    }

    public static Quality toQuality(String text){
        try {
            return Quality.valueOf(text.trim().toUpperCase());
        } catch(RuntimeException e){
            throw new InvalidOperationException("La calidad debe ser A, B o C: '" + text + "'.");
        }
    }
}
