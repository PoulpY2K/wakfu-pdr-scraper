package fr.fumbus.wakfupdrscraper.models.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "playwright")
public class PlaywrightProperties {
    @NotEmpty
    @Size(min = 1)
    private Set<String> userAgents;

    @NotNull
    private Boolean headless;
}
