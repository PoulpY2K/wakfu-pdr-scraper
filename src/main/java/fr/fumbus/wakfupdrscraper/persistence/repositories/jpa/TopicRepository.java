package fr.fumbus.wakfupdrscraper.persistence.repositories.jpa;

import fr.fumbus.wakfupdrscraper.persistence.entities.TopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
interface TopicRepository extends JpaRepository<TopicEntity, Long> {
}
