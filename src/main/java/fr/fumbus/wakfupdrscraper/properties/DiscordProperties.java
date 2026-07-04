package fr.fumbus.wakfupdrscraper.properties;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Data
@Configuration
@RequiredArgsConstructor
public class DiscordProperties {

    @Value("${discord.name}")
    private String name;

    @Value("${discord.profile-picture-url}")
    private String profilePictureUrl;

    @Value("${discord.webhook.global.id}")
    private String globalWebhookId;

    @Value("${discord.webhook.global.token}")
    private String globalWebhookToken;

    @Value("${discord.webhook.board.id}")
    private String boardWebhookId;

    @Value("${discord.webhook.board.token}")
    private String boardWebhookToken;

    @Value("${discord.webhook.rumors.id}")
    private String rumorsWebhookId;

    @Value("${discord.webhook.rumors.token}")
    private String rumorsWebhookToken;
}
