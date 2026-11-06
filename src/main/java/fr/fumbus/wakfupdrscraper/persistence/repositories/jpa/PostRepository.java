package fr.fumbus.wakfupdrscraper.persistence.repositories.jpa;

import fr.fumbus.wakfupdrscraper.persistence.entities.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

interface PostRepository extends JpaRepository<PostEntity, Long> {

    @Query("select p.id from PostEntity p where p.id in :ids")
    Set<Long> findExistingIds(Set<Long> ids);
}