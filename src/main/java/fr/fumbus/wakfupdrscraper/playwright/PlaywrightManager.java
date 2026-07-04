package fr.fumbus.wakfupdrscraper.playwright;

import fr.fumbus.wakfupdrscraper.properties.PlaywrightProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Log4j2
@Component
@RequiredArgsConstructor
public class PlaywrightManager {
    private final PlaywrightProperties properties;

}

