package fr.fumbus.wakfupdrscraper.configurations;

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
public class WakfuConfiguration {

    @Value("${wakfu.forum.url}")
    private String forumUrl;

    @Value("${wakfu.forum.rp}")
    private String forumRoleplayPath;
}
