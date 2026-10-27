package fr.fumbus.wakfupdrscraper.models;

import fr.fumbus.wakfupdrscraper.models.errors.ErrorsCode;
import lombok.Builder;
import lombok.Data;

import java.util.Comparator;
import java.util.Objects;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Data
@Builder
public class WakfuPdrScraperError implements Comparable<WakfuPdrScraperError> {

    private ErrorsCode errorCode = ErrorsCode.ERR01;
    private String description = ErrorsCode.ERR01.description;

    @Override
    public int compareTo(WakfuPdrScraperError o) {
        return Objects.compare(errorCode, o.errorCode,
                Comparator.nullsLast(Comparator.comparingInt(ErrorsCode::ordinal)));
    }
}
