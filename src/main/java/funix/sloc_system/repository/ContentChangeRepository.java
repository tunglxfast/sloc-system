package funix.sloc_system.repository;

import funix.sloc_system.entity.ContentChangeTemporary;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ContentChangeRepository extends JpaRepository<ContentChangeTemporary, Long> {

    Optional<ContentChangeTemporary> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    Set<ContentChangeTemporary> findByEntityId(Long entityId);

    List<ContentChangeTemporary> findByAction(ContentAction action);
    
    @Modifying
    @Query("DELETE FROM ContentChangeTemporary c WHERE c.entityType = :entityType AND c.entityId = :entityId")
    void deleteByEntityTypeAndEntityId(
            @Param("entityType") EntityType entityType, 
            @Param("entityId") Long entityId);
}
