package Model;

import java.time.LocalDate;

public interface Perishable {
    int getShelfLifeDays();

    LocalDate getEntryDate();

    default LocalDate getExpirationDate(){
        return getEntryDate().plusDays(getShelfLifeDays());
    }

    default boolean isExpired(LocalDate today){
        return today.isAfter(getExpirationDate());
    }
}
