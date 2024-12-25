package funix.sloc_system.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.ContentChangeTemporary;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
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
    private QuestionRepository questionRepository;
    @Autowired
    private AnswerRepository answerRepository;
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

    /**
     * Check course is available or not.
     * @param courseId
     * @return
     */
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
    public void saveEntityChanges(EntityType entityType, String json, Long entityId, ContentAction action, Long instructorId) throws JsonProcessingException {
        ContentChangeTemporary changeTemporary = contentChangeRepository
                .findByEntityTypeAndEntityId(entityType, entityId)
                .orElse(new ContentChangeTemporary());

        changeTemporary.setEntityType(entityType);
        changeTemporary.setEntityId(entityId);
        changeTemporary.setAction(action);
        changeTemporary.setChanges(json);
        changeTemporary.setUpdatedBy(instructorId);
        changeTemporary.setChangeTime(LocalDateTime.now());
        contentChangeRepository.save(changeTemporary);
    }
}
