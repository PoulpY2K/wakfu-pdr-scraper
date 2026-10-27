package fr.fumbus.wakfupdrscraper.models.exceptions.technical;

import fr.fumbus.wakfupdrscraper.models.WakfuPdrScraperError;
import fr.fumbus.wakfupdrscraper.models.exceptions.WakfuPdrScraperException;

import java.util.List;

import static fr.fumbus.wakfupdrscraper.models.errors.ErrorsCode.ERR01;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

public class CommonTechnicalException extends WakfuPdrScraperException {
    private static final String DEFAULT_MESSAGE = "An error occurred while processing.";

    private static final WakfuPdrScraperError DEFAULT_ERROR = WakfuPdrScraperError.builder()
            .errorCode(ERR01)
            .description(ERR01.description)
            .build();

    public CommonTechnicalException() {
        super(DEFAULT_ERROR, DEFAULT_MESSAGE);
    }

    public CommonTechnicalException(final String message) {
        super(DEFAULT_ERROR, message);
    }

    public CommonTechnicalException(final WakfuPdrScraperError error) {
        super(error);
    }

    public CommonTechnicalException(final WakfuPdrScraperError error, final String message) {
        super(error, message);
    }

    public CommonTechnicalException(final List<WakfuPdrScraperError> errors) {
        super(errors);
    }
}
