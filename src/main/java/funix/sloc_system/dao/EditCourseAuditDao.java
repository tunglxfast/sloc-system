package funix.sloc_system.dao;

import funix.sloc_system.entity.EditCourseAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EditCourseAuditDao extends JpaRepository<EditCourseAudit, Long> {

    List<EditCourseAudit> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<EditCourseAudit> findByAction(String action);

    List<EditCourseAudit> findByEntityTypeAndEntityIdAndStatusOrderByChangeTimeAsc(
            String entityType, Long entityId, String status);

    List<EditCourseAudit> findByEntityTypeAndEntityIdAndStatusOrderByChangeTimeDesc(
            String entityType, Long entityId, String status);
}
