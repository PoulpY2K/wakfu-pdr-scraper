package fr.fumbus.wakfupdrscraper.persistence.repositories.jpa;

import fr.fumbus.wakfupdrscraper.persistence.entities.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {
    Set<AuthorEntity> findByHandleIn(Set<String> handles);
}