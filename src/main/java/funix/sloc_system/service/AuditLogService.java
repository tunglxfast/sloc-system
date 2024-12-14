package funix.sloc_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dao.AuditLogDao;
import funix.sloc_system.entity.AuditLog;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogDao auditLogDao;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void saveEditingCourse(String entityType, Course editingCourse, String action, User updatedBy) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(editingCourse);
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(editingCourse.getId());
        log.setAction(action);
        log.setChanges(json);
        log.setUpdatedBy(updatedBy);
        log.setChangeTime(LocalDateTime.now());
        auditLogDao.save(log);
    }
}
