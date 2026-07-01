package fr.fumbus.wakfupdrscraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WakfuPdrScraperApplication {

    static void main(String[] args) {
        SpringApplication.run(WakfuPdrScraperApplication.class, args);
    }
}
