package fr.fumbus.wakfupdrscraper.models.properties;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "scraping")
public class ScrapingProperties {
    @Min(1)
    private int categoryPageLimit = 3;

    @Min(1)
    private int topicPageLimit = 1000;
}
