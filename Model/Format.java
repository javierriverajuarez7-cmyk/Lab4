package Model;

import java.util.Locale;

public final class Format {

    private Format(){}

    public static String money(double value){
        return String.format(Locale.US, "Q%,.2f", value);
    }

    public static String kg(double value){
        return String.format(Locale.US, "%.2f kg", value);
    }
}
