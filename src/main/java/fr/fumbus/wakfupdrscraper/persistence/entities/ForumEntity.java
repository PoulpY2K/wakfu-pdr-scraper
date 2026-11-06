package fr.fumbus.wakfupdrscraper.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.domain.Persistable;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
@Getter
@MappedSuperclass
public abstract class ForumEntity implements Persistable<Long> {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Transient
    private boolean isNew = true;

    protected ForumEntity() {}

    protected ForumEntity(long id) {
        this.id = id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}
