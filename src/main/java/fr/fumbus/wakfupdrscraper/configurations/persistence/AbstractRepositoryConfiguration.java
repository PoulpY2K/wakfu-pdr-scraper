package fr.fumbus.wakfupdrscraper.configurations.persistence;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
public abstract class AbstractRepositoryConfiguration {
    protected LocalContainerEntityManagerFactoryBean createEntityManager(final DataSource dataSource, final String... packagesToScan) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");
        entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter);

        Properties properties = new Properties();
        properties.put("hibernate.generate_statistics", "false");
        properties.put("hibernate.boot.allow_jdbc_metadata_access", false);
        properties.put("hibernate.transaction.jta.platform", false);
        entityManagerFactoryBean.setJpaProperties(properties);
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setPackagesToScan(packagesToScan);

        return entityManagerFactoryBean;
    }

    protected PlatformTransactionManager createTransactionManager(final LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(localContainerEntityManagerFactoryBean.getObject());
        return jpaTransactionManager;
    }
}
