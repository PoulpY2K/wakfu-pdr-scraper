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
public class WakfuProperties {

    @Value("${wakfu.forum.url}")
    @NotBlank
    private String forumUrl;

    @Value("${wakfu.forum.rp.path}")
    @NotBlank
    private String forumRoleplayPath;

    @Value("${wakfu.forum.rumors.path}")
    @NotBlank
    private String forumRoleplayRumorsPath;

    @Value("${wakfu.forum.board.path}")
    @NotBlank
    private String forumRoleplayBoardPath;
}
