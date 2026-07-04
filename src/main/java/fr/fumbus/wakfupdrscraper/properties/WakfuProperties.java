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
public class WakfuProperties {

    @Value("${wakfu.forum.url}")
    private String forumUrl;

    @Value("${wakfu.forum.rp.path}")
    private String forumRoleplayPath;

    @Value("${wakfu.forum.rumors.path}")
    private String forumRoleplayRumorsPath;

    @Value("${wakfu.forum.board.path}")
    private String forumRoleplayBoardPath;
}
