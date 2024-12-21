package funix.sloc_system.repository;

import funix.sloc_system.entity.ContentChangeTemporary;
import funix.sloc_system.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentChangeRepository extends JpaRepository<ContentChangeTemporary, Long> {

    Optional<ContentChangeTemporary> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    List<ContentChangeTemporary> findByAction(String action);
}
