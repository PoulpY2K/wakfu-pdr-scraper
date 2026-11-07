package fr.fumbus.wakfupdrscraper.models.exceptions;

import fr.fumbus.wakfupdrscraper.models.WakfuPdrScraperError;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
public class WakfuPdrScraperException extends RuntimeException {

    private final List<WakfuPdrScraperError> errors = new ArrayList<>();

    public WakfuPdrScraperException(final WakfuPdrScraperError error) {
        super();
        this.errors.add(error);
    }

    public WakfuPdrScraperException(final WakfuPdrScraperError error, final String message) {
        super(message);
        this.errors.add(error);
    }

    public WakfuPdrScraperException(final List<WakfuPdrScraperError> errors) {
        super();
        this.errors.addAll(errors);
    }

    public WakfuPdrScraperException(final List<WakfuPdrScraperError> errors, final String message) {
        super(message);
        this.errors.addAll(errors);
    }
}
