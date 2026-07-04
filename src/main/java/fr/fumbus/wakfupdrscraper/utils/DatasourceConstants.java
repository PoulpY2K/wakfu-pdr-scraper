package fr.fumbus.wakfupdrscraper.utils;

import lombok.experimental.UtilityClass;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@UtilityClass
public class DatasourceConstants {
    public static final String PDR_BASE_PACKAGE = "fr.fumbus.wakfupdrscraper.persistence.repositories";
    public static final String PDR_ENTITY_PACKAGE = "fr.fumbus.wakfupdrscraper.persistence.entities";

    public static final String PDR_DATA_SOURCE_PREFIX = "datasources.pdr";
    public static final String PDR_DATA_SOURCE = "pdrDataSource";
    public static final String PDR_ENTITY_MANAGER_FACTORY = "pdrEntityManagerFactory";
    public static final String PDR_TRANSACTION_MANAGER = "pdrTransactionManager";
}
