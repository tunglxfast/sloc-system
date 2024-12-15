package funix.sloc_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dao.EditCourseAuditDao;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.EditCourseAudit;
import funix.sloc_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EditCourseAuditService {

    @Autowired
    private EditCourseAuditDao editCourseAuditDao;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void saveEditingCourse(String entityType, Course editingCourse, String action, User updatedBy) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(editingCourse);
        EditCourseAudit log = new EditCourseAudit();
        log.setEntityType(entityType);
        log.setEntityId(editingCourse.getId());
        log.setAction(action);
        log.setChanges(json);
        log.setUpdatedBy(updatedBy);
        log.setChangeTime(LocalDateTime.now());
        editCourseAuditDao.save(log);
    }

    @Transactional
    public Optional<EditCourseAudit> getLatestAudits(String entityType, Long entityId, String status) {
        List<EditCourseAudit> courseAudits = editCourseAuditDao.
                findByEntityTypeAndEntityIdAndStatusOrderByChangeTimeDesc(entityType, entityId, status);
        return Optional.ofNullable(courseAudits.get(0));
    }
}
