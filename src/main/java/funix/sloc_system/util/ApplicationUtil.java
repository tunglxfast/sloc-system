package funix.sloc_system.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.*;
import funix.sloc_system.entity.*;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.mapper.*;
import funix.sloc_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ApplicationUtil {

    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContentChangeRepository contentChangeRepository;
    @Autowired
    private ObjectMapper objectMapper;
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

    public Topic findNextTopic(Long topicId){
        Topic currentTopic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));


        int nextTopicSequence = currentTopic.getSequence() + 1;
        Chapter currentChapter = currentTopic.getChapter();

        List<Topic> topics = currentChapter.getTopics();
        for (Topic topic: topics) {
            if (topic.getSequence() == nextTopicSequence) {
                return topic;
            }
        }
        // nếu không có topic nào sequence topic+1 -> topic cuối của chapter -> qua chapter mới
        int nextChapterSequence = currentChapter.getSequence() + 1;
        Course currentCourse = currentChapter.getCourse();

        List<Chapter> chapters = chapterRepository.findByCourseIdOrderBySequence(currentCourse.getId());
        for (Chapter chapter: chapters) {
            if (chapter.getSequence() == nextChapterSequence) {
                return chapter.getTopics().get(0);
            }
        }

        // nếu vẫn không tìm được -> null
        return null;
    }

    public boolean isCourseReady(Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return false;
        }
        ContentStatus contentStatus = course.getContentStatus();
        return contentStatus != ContentStatus.DRAFT && contentStatus != ContentStatus.READY_TO_REVIEW;
    }

    /**
     * Save entity changes to temp table.
     * Only called for published courses when modifying existing content.
     * The changes JSON will contain the complete state of the entity after changes.
     */
    @Transactional
    public void saveEntityChanges(EntityType entityType, Object editingDTO, Object originalEntity, ContentAction action, Long instructorId) throws JsonProcessingException {
        User instructor = userRepository.findById(instructorId).orElse(null);
        Long entityId;
        // Update original entity with changes
        switch (entityType) {
            case COURSE:
                Course course = (Course) originalEntity;
                entityId = course.getId();
                Course updatedCourse = courseMapper.toEntity((CourseDTO) editingDTO);
                course.updateWithOtherCourse(updatedCourse);
                editingDTO = courseMapper.toDTO(course);
                break;
            case CHAPTER:
                Chapter chapter = (Chapter) originalEntity;
                entityId = chapter.getId();
                Chapter updatedChapter = chapterMapper.toEntity((ChapterDTO) editingDTO);
                chapter.updateWithOtherChapter(updatedChapter);
                editingDTO = chapterMapper.toDTO(chapter);
                break;
            case TOPIC:
                Topic topic = (Topic) originalEntity;
                entityId = topic.getId();
                Topic updatedTopic = topicMapper.toEntity((TopicDTO) editingDTO);
                topic.updateWithOtherTopic(updatedTopic);
                editingDTO = topicMapper.toDTO(topic);
                break;
            case QUESTION:
                Question question = (Question) originalEntity;
                entityId = question.getId();
                Question updatedQuestion = questionMapper.toEntity((QuestionDTO) editingDTO);
                question.updateWithOtherQuestion(updatedQuestion);
                editingDTO = questionMapper.toDTO(question);
                break;
            default:
                Answer answer = (Answer) originalEntity;
                entityId = answer.getId();
                Answer updatedAnswer = answerMapper.toEntity((AnswerDTO) editingDTO);
                answer.updateWithOtherAnswer(updatedAnswer);
                editingDTO = answerMapper.toDTO(answer);
                break;
        }

        // Convert updated entity to JSON
        String json = objectMapper.writeValueAsString(editingDTO);

        // Save to temporary table
        contentChangeRepository.findById(1L);
        Optional<ContentChangeTemporary> temporaryOptional = contentChangeRepository.findByEntityTypeAndEntityId(entityType, entityId);
        ContentChangeTemporary changeTemporary;
        if (temporaryOptional.isPresent()) {
            changeTemporary = temporaryOptional.get();
        } else {
            changeTemporary = new ContentChangeTemporary();
        }
        changeTemporary.setEntityType(entityType);
        changeTemporary.setEntityId(entityId);
        changeTemporary.setAction(action);
        changeTemporary.setChanges(json);
        changeTemporary.setUpdatedBy(instructor.getId());
        changeTemporary.setChangeTime(LocalDateTime.now());
        contentChangeRepository.save(changeTemporary);
    }
}
