package funix.sloc_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.*;
import funix.sloc_system.entity.*;
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
     * Delete temporary changes for a specific entity
     */
    @Transactional
    public void deleteEntityChanges(EntityType entityType, Long entityId) {
        changeTemporaryDao.deleteByEntityTypeAndEntityId(entityType, entityId);
    }

    /**
     * Get temporary changes for a specific entity
     */
    @Transactional
    public Optional<ContentChangeTemporary> getEntityEditing(EntityType entityType, Long entityId) {
        return changeTemporaryDao.findByEntityTypeAndEntityId(entityType, entityId);
    }

    /**
     * Save entity changes to temp table.
     * Only called for published courses when modifying existing content.
     * The changes JSON will only include database-relevant attributes,
     * as defined by @JsonIgnore annotations in the DTO classes.
     */
    @Transactional
    public void saveEntityChanges(EntityType entityType, Long entityId, Object editingEntity, ContentAction action, Long instructorId) throws JsonProcessingException {
        User instructor = userRepository.findById(instructorId).orElse(null);
        String json = objectMapper.writeValueAsString(editingEntity);
        ContentChangeTemporary changeTemporary = changeTemporaryDao
                .findByEntityTypeAndEntityId(entityType, entityId)
                .orElse(new ContentChangeTemporary());

        changeTemporary.setEntityType(entityType);
        changeTemporary.setEntityId(entityId);
        changeTemporary.setAction(action);
        changeTemporary.setChanges(json);
        changeTemporary.setUpdatedBy(instructor);
        changeTemporary.setChangeTime(LocalDateTime.now());
        changeTemporaryDao.save(changeTemporary);
    }

    /**
     * Save course changes to temp table.
     */
    @Transactional
    public void saveEditingCourse(CourseDTO editingCourse, ContentAction action, Long instructorId) throws JsonProcessingException {
        saveEntityChanges(EntityType.COURSE, editingCourse.getId(), editingCourse, action, instructorId);
    }

    /**
     * Save chapter changes to temp table.
     */
    @Transactional
    public void saveEditingChapter(ChapterDTO editingChapter, ContentAction action, Long instructorId) throws JsonProcessingException {
        saveEntityChanges(EntityType.CHAPTER, editingChapter.getId(), editingChapter, action, instructorId);
    }

    /**
     * Save topic changes to temp table.
     */
    @Transactional
    public void saveEditingTopic(TopicDTO editingTopic, ContentAction action, Long instructorId) throws JsonProcessingException {
        saveEntityChanges(EntityType.TOPIC, editingTopic.getId(), editingTopic, action, instructorId);
    }

    /**
     * Save question changes to temp table.
     * This includes both the question and its answers as a single unit.
     * The QuestionDTO should include the complete list of answers.
     */
    @Transactional
    public void saveEditingQuestion(QuestionDTO editingQuestion, ContentAction action, Long instructorId) throws JsonProcessingException {
        // Save the question with its answers included in the JSON
        saveEntityChanges(EntityType.QUESTION, editingQuestion.getId(), editingQuestion, action, instructorId);
    }

    /**
     * Delete all changes related to a course when it's approved.
     * This includes changes to the course itself, chapters, topics, and questions (with their answers).
     */
    @Transactional
    public void deleteAllCourseChanges(Course course) {
        // Delete course changes
        deleteEntityChanges(EntityType.COURSE, course.getId());

        // Delete chapter changes
        for (Chapter chapter : course.getChapters()) {
            deleteEntityChanges(EntityType.CHAPTER, chapter.getId());

            // Delete topic changes
            for (Topic topic : chapter.getTopics()) {
                deleteEntityChanges(EntityType.TOPIC, topic.getId());

                // Delete question changes (answers are included in question JSON)
                if (topic.getQuestions() != null) {
                    for (Question question : topic.getQuestions()) {
                        deleteEntityChanges(EntityType.QUESTION, question.getId());
                    }
                }
            }
        }
    }
}
