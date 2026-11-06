package fr.fumbus.wakfupdrscraper.persistence.repositories.jpa;

import fr.fumbus.wakfupdrscraper.models.NotificationStatus;
import fr.fumbus.wakfupdrscraper.persistence.entities.NotificationEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @EntityGraph(attributePaths = {"postEntity", "postEntity.topic", "postEntity.authorEntity"})
    Set<NotificationEntity> findByStatusOrderByCreatedAtAsc(NotificationStatus status, Limit limit);
}
