package Model;

import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface LotCreator {
    Lot create(String code, String producer, LocalDate entryDate, double kg, Quality quality, List<String> values);
}
