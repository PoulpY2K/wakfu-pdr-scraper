package fr.fumbus.wakfupdrscraper.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Configuration
public class ExecutorServiceConfiguration {

    @Bean("executorService")
    public ExecutorService securityContextAwareExecutorService() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("scraper-", 0).factory());
    }
}
