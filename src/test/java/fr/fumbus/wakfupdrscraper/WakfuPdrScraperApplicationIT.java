package fr.fumbus.wakfupdrscraper;

import fr.fumbus.wakfupdrscraper.persistence.configurations.PostgresTestConfiguration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@RequiredArgsConstructor
@ActiveProfiles({"test"})
@Import(PostgresTestConfiguration.class)
class WakfuPdrScraperApplicationIT {

    private final ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

}
