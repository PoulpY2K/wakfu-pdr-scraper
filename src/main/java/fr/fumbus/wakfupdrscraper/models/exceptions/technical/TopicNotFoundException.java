package fr.fumbus.wakfupdrscraper.models.exceptions.technical;

import fr.fumbus.wakfupdrscraper.models.WakfuPdrScraperError;

import static fr.fumbus.wakfupdrscraper.models.errors.ErrorsCode.TOP01;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
public class TopicNotFoundException extends CommonTechnicalException {
    public TopicNotFoundException(String message) {
        super(WakfuPdrScraperError.builder()
                .errorCode(TOP01)
                .description(TOP01.description + " : " + message)
                .build());
    }
}
