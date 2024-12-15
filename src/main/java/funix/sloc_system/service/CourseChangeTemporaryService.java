package funix.sloc_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dao.CourseChangeTemporaryDao;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.CourseChangeTemporary;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.CourseChangeAction;
import funix.sloc_system.enums.CourseStatus;
import funix.sloc_system.enums.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CourseChangeTemporaryService {

    @Autowired
    private CourseChangeTemporaryDao changeTemporaryDao;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void saveEditingCourse(Course editingCourse, CourseChangeAction action, User updatedBy) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(editingCourse);
        CourseChangeTemporary changeTemporary = changeTemporaryDao
                .findByEntityTypeAndEntityId(EntityType.COURSE, editingCourse.getId())
                .orElse(new CourseChangeTemporary());

        changeTemporary.setEntityType(EntityType.COURSE);
        changeTemporary.setEntityId(editingCourse.getId());
        changeTemporary.setAction(action);
        changeTemporary.setChanges(json);
        changeTemporary.setStatus(CourseStatus.PENDING_EDIT);
        changeTemporary.setUpdatedBy(updatedBy);
        changeTemporary.setChangeTime(LocalDateTime.now());
        changeTemporaryDao.save(changeTemporary);
    }

    @Transactional
    public Optional<CourseChangeTemporary> getCourseEditing(EntityType entityType, Long entityId) {
        return changeTemporaryDao.findByEntityTypeAndEntityId(entityType, entityId);
    }
}
