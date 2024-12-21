package funix.sloc_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.ContentChangeTemporary;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.repository.ContentChangeRepository;
import funix.sloc_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ContentChangeService {

    @Autowired
    private ContentChangeRepository changeTemporaryDao;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    /**
     * Save course to temp table.
     * @param editingCourse
     * @param action
     * @param instructorId
     * @throws JsonProcessingException
     */
    @Transactional
    public void saveEditingCourse(CourseDTO editingCourse, ContentAction action, Long instructorId) throws JsonProcessingException {
        User instructor = userRepository.findById(instructorId).orElse(null);
        String json = objectMapper.writeValueAsString(editingCourse);
        ContentChangeTemporary changeTemporary = changeTemporaryDao
                .findByEntityTypeAndEntityId(EntityType.COURSE, editingCourse.getId())
                .orElse(new ContentChangeTemporary());

        changeTemporary.setEntityType(EntityType.COURSE);
        changeTemporary.setEntityId(editingCourse.getId());
        changeTemporary.setAction(action);
        changeTemporary.setChanges(json);
        changeTemporary.setUpdatedBy(instructor);
        changeTemporary.setChangeTime(LocalDateTime.now());
        changeTemporaryDao.save(changeTemporary);
    }

    @Transactional
    public Optional<ContentChangeTemporary> getCourseEditing(EntityType entityType, Long entityId) {
        return changeTemporaryDao.findByEntityTypeAndEntityId(entityType, entityId);
    }
}
