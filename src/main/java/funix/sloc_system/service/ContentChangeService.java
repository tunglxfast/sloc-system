package funix.sloc_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.*;
import funix.sloc_system.entity.*;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.repository.*;
import funix.sloc_system.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContentChangeService {

    @Autowired
    private ContentChangeRepository changeTemporaryDao;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private ChapterMapper chapterMapper;
    @Autowired
    private TopicMapper topicMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private AnswerMapper answerMapper;

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
     * The changes JSON will contain the complete state of the entity after changes.
     */
    @Transactional
    public void saveEntityChanges(EntityType entityType, Object editingDTO, Object originalEntity, ContentAction action, Long instructorId) throws JsonProcessingException {
        User instructor = userRepository.findById(instructorId).orElse(null);
        Long entityId = ((Course) originalEntity).getId();
        // Update original entity with changes
        switch (entityType) {
            case COURSE:
                Course course = (Course) originalEntity;
                Course updatedCourse = courseMapper.toEntity((CourseDTO) editingDTO);
                course.updateWithOtherCourse(updatedCourse);
                editingDTO = courseMapper.toDTO(course);
                break;
            case CHAPTER:
                Chapter chapter = (Chapter) originalEntity;
                Chapter updatedChapter = chapterMapper.toEntity((ChapterDTO) editingDTO);
                chapter.updateWithOtherChapter(updatedChapter);
                editingDTO = chapterMapper.toDTO(chapter);
                break;
            case TOPIC:
                Topic topic = (Topic) originalEntity;
                Topic updatedTopic = topicMapper.toEntity((TopicDTO) editingDTO);
                topic.updateWithOtherTopic(updatedTopic);
                editingDTO = topicMapper.toDTO(topic);
                break;
            case QUESTION:
                Question question = (Question) originalEntity;
                Question updatedQuestion = questionMapper.toEntity((QuestionDTO) editingDTO);
                question.updateWithOtherQuestion(updatedQuestion);
                editingDTO = questionMapper.toDTO(question);
                break;
            case ANSWER:
                Answer answer = (Answer) originalEntity;
                Answer updatedAnswer = answerMapper.toEntity((AnswerDTO) editingDTO);
                answer.updateWithOtherAnswer(updatedAnswer);
                editingDTO = answerMapper.toDTO(answer);
                break;
        }

        // Convert updated entity to JSON
        String json = objectMapper.writeValueAsString(editingDTO);

        // Save to temporary table
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
    public void saveEditingCourse(Course originalCourse, CourseDTO editingCourse, ContentAction action, Long instructorId) throws JsonProcessingException {
        saveEntityChanges(EntityType.COURSE, editingCourse, originalCourse, action, instructorId);
    }

    /**
     * Save chapter changes to temp table.
     */
    @Transactional
    public void saveEditingChapter(Chapter originalChapter, ChapterDTO editingChapter, ContentAction action, Long instructorId) throws JsonProcessingException {
        saveEntityChanges(EntityType.CHAPTER, editingChapter, originalChapter, action, instructorId);
    }

    /**
     * Save topic changes to temp table.
     */
    @Transactional
    public void saveEditingTopic(Topic originalTopic, TopicDTO editingTopic, ContentAction action, Long instructorId) throws JsonProcessingException {
        saveEntityChanges(EntityType.TOPIC, editingTopic, originalTopic, action, instructorId);
    }

    /**
     * Save question changes to temp table.
     * This includes both the question and its answers as a single unit.
     * The QuestionDTO should include the complete list of answers.
     */
    @Transactional
    public void saveEditingQuestion(Question originalQuestion, QuestionDTO editingQuestion, ContentAction action, Long instructorId) throws JsonProcessingException {
        saveEntityChanges(EntityType.QUESTION, editingQuestion, originalQuestion, action, instructorId);
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

    /**
     * Get all changes for a course and its child entities
     */
    @Transactional
    public List<ContentChangeTemporary> getAllCourseChanges(Course course) {
        List<ContentChangeTemporary> allChanges = new ArrayList<>();

        // Get course changes
        Optional<ContentChangeTemporary> courseChange = getEntityEditing(EntityType.COURSE, course.getId());
        courseChange.ifPresent(allChanges::add);

        // Get chapter changes
        for (Chapter chapter : course.getChapters()) {
            Optional<ContentChangeTemporary> chapterChange = getEntityEditing(EntityType.CHAPTER, chapter.getId());
            chapterChange.ifPresent(allChanges::add);

            // Get topic changes
            for (Topic topic : chapter.getTopics()) {
                Optional<ContentChangeTemporary> topicChange = getEntityEditing(EntityType.TOPIC, topic.getId());
                topicChange.ifPresent(allChanges::add);

                // Get question changes
                if (topic.getQuestions() != null) {
                    for (Question question : topic.getQuestions()) {
                        Optional<ContentChangeTemporary> questionChange = getEntityEditing(EntityType.QUESTION, question.getId());
                        questionChange.ifPresent(allChanges::add);
                    }
                }
            }
        }

        return allChanges;
    }
}
