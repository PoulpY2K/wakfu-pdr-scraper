package fr.fumbus.wakfupdrscraper.models.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
public class ScraperProperties {

    @Value("${scraper.limits.category}")
    @Min(1)
    @NotNull
    private int categoryPageLimit;

    @Value("${scraper.limits.topic}")
    @Min(1)
    @NotNull
    private int topicPageLimit;
}
