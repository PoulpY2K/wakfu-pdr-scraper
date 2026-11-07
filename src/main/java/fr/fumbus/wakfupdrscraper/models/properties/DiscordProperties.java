package fr.fumbus.wakfupdrscraper.models.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Data
@Validated
@Configuration
public class DiscordProperties {

    @Value("${discord.name}")
    @NotBlank
    private String name;

    @Value("${discord.profile-picture-url}")
    @NotBlank
    private String profilePictureUrl;

    @Value("${discord.webhook.global.id}")
    @NotBlank
    private String globalWebhookId;

    @Value("${discord.webhook.global.token}")
    @NotBlank
    private String globalWebhookToken;

    @Value("${discord.webhook.board.id}")
    @NotBlank
    private String boardWebhookId;

    @Value("${discord.webhook.board.token}")
    @NotBlank
    private String boardWebhookToken;

    @Value("${discord.webhook.rumors.id}")
    @NotBlank
    private String rumorsWebhookId;

    @Value("${discord.webhook.rumors.token}")
    @NotBlank
    private String rumorsWebhookToken;
}
