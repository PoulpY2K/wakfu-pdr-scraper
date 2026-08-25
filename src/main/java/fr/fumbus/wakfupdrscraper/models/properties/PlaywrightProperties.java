package fr.fumbus.wakfupdrscraper.models.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "playwright")
public class PlaywrightProperties {
    private Set<String> userAgents;
    private Boolean headless;
}
