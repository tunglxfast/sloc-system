package funix.sloc_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dao.CourseChangeTemporaryDao;
import funix.sloc_system.dao.UserDao;
import funix.sloc_system.dto.CourseDTO;
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
    @Autowired
    private UserDao userDao;

    /**
     * Save course to temp table.
     * @param editingCourse
     * @param action
     * @param instructorId
     * @throws JsonProcessingException
     */
    @Transactional
    public void saveEditingCourse(CourseDTO editingCourse, CourseChangeAction action, Long instructorId) throws JsonProcessingException {
        User instructor = userDao.findById(instructorId).orElse(null);
        String json = objectMapper.writeValueAsString(editingCourse);
        CourseChangeTemporary changeTemporary = changeTemporaryDao
                .findByEntityTypeAndEntityId(EntityType.COURSE, editingCourse.getId())
                .orElse(new CourseChangeTemporary());

        changeTemporary.setEntityType(EntityType.COURSE);
        changeTemporary.setEntityId(editingCourse.getId());
        changeTemporary.setAction(action);
        changeTemporary.setChanges(json);
        changeTemporary.setStatus(CourseStatus.PENDING_EDIT);
        changeTemporary.setUpdatedBy(instructor);
        changeTemporary.setChangeTime(LocalDateTime.now());
        changeTemporaryDao.save(changeTemporary);
    }

    @Transactional
    public Optional<CourseChangeTemporary> getCourseEditing(EntityType entityType, Long entityId) {
        return changeTemporaryDao.findByEntityTypeAndEntityId(entityType, entityId);
    }
}
