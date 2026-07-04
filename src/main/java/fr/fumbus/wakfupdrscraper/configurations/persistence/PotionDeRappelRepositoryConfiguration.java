package fr.fumbus.wakfupdrscraper.configurations.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static fr.fumbus.wakfupdrscraper.utils.DatasourceConstants.*;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
@Configuration
@ConditionalOnClass(DataSource.class)
@EnableJpaRepositories(
        basePackages = PDR_BASE_PACKAGE,
        entityManagerFactoryRef = PDR_ENTITY_MANAGER_FACTORY,
        transactionManagerRef = PDR_TRANSACTION_MANAGER
)
public class PotionDeRappelRepositoryConfiguration extends AbstractRepositoryConfiguration {

    @Bean
    @ConfigurationProperties(prefix = PDR_DATA_SOURCE_PREFIX)
    public DataSource pdrDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean pdrEntityManagerFactory(
            @Autowired @Qualifier(PDR_DATA_SOURCE) DataSource dataSource) {
        return createEntityManager(dataSource, PDR_ENTITY_PACKAGE);
    }

    @Bean
    @Primary
    public PlatformTransactionManager pdrTransactionManager(
            @Autowired @Qualifier(PDR_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return createTransactionManager(entityManagerFactory);
    }

}
