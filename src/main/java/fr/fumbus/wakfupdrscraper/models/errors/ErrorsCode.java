package fr.fumbus.wakfupdrscraper.models.errors;

import lombok.RequiredArgsConstructor;

import static fr.fumbus.wakfupdrscraper.utils.Constants.DEFAULT_DESCRIPTION;
import static fr.fumbus.wakfupdrscraper.utils.Constants.TOP01_DESCRIPTION;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@RequiredArgsConstructor
public enum ErrorsCode {

    ERR01(DEFAULT_DESCRIPTION),
    TOP01(TOP01_DESCRIPTION);

    public final String description;
}
