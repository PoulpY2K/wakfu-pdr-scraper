package fr.fumbus.wakfupdrscraper.configurations;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Data
@Configuration
@RequiredArgsConstructor
public class PlaywrightConfiguration {

    @Value("${playwright.user-agents}")
    private Set<String> userAgents;

    @Value("${playwright.headless}")
    private Boolean headless;
}
