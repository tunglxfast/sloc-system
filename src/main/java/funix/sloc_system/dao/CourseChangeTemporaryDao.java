package funix.sloc_system.dao;

import funix.sloc_system.entity.CourseChangeTemporary;
import funix.sloc_system.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseChangeTemporaryDao extends JpaRepository<CourseChangeTemporary, Long> {

    Optional<CourseChangeTemporary> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    List<CourseChangeTemporary> findByAction(String action);
}
