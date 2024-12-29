package funix.sloc_system.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.TopicDTO;
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
public class AppUtil {

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
    @Autowired
    private EnrollmentMapper enrollmentMapper;

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
    public void saveContentChange(String json, Long entityId) {
        this.saveContentChange(json, entityId, null);
    }

    @Transactional
    public void saveContentChange(String json, Long entityId, Long instructorId) {
        ContentChangeTemporary changeTemporary = contentChangeRepository
                .findByEntityTypeAndEntityId(EntityType.COURSE, entityId)
                .orElse(new ContentChangeTemporary());

        changeTemporary.setEntityType(EntityType.COURSE);
        changeTemporary.setEntityId(entityId);
        changeTemporary.setAction(ContentAction.UPDATE);
        changeTemporary.setChanges(json);
        changeTemporary.setChangeTime(LocalDateTime.now());

        if (instructorId != null) {
            changeTemporary.setUpdatedBy(instructorId);
        }

        contentChangeRepository.save(changeTemporary);
    }

    /**
     * Find course's changes from ContentChangeTemporary
     * then update with current Course.
     * @param id Course Id
     */
    public CourseDTO getEditingCourseDTO(Long id) throws Exception {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Editing course not found"));
        String editingJson = getEditingJson(EntityType.COURSE, id);

        if (course.getContentStatus().equals(ContentStatus.DRAFT) || editingJson == null) {
            return courseMapper.toDTO(course);
        } else {
            try {
                CourseDTO editingDTO = objectMapper.readValue(editingJson, CourseDTO.class);
                // set enrollments because this attribute is JsonIgnore
                editingDTO.setEnrollments(enrollmentMapper.toDTO(course.getEnrollments()));
                return editingDTO;
            } catch (Exception e) {
                throw new Exception("Error while get editing course: " + course.getTitle()
                        + ", please contact support center.");
            }
        }
    }

    /**
     * Get editing changes for ContentChangeTemporary
     */
    public String getEditingJson(EntityType entityType, Long id) {
        ContentChangeTemporary changeTemporary = contentChangeRepository.findByEntityTypeAndEntityId(
                entityType,
                id).orElse(null);
        if (changeTemporary == null) {
            return null;
        } else {
            return changeTemporary.getChanges();
        }
    }


    public static ChapterDTO getSelectChapterDTO(CourseDTO courseDTO, Long chapterId) {
        for (ChapterDTO chapterDTO : courseDTO.getChapters()) {
            if (chapterDTO.getId().equals(chapterId)) {
                return chapterDTO;
            }
        }
        // throw exeption if not found any match with chapterId
        throw new IllegalArgumentException(String.format("Chapter with id: %d not found", chapterId));
    }

    public static TopicDTO getSelectTopicDTO(ChapterDTO chapterDTO, Long topicId) {
        for (TopicDTO topicDTO : chapterDTO.getTopics()) {
            if (topicDTO.getId().equals(topicId)) {
                return topicDTO;
            }
        }
        // throw exeption if not found any match with chapterId
        throw new IllegalArgumentException(String.format("Topic with id: %d not found", topicId));
    }

    public String getNextTopicUrl(Long topicId, Long courseId) {
        Topic nextTopic = findNextTopic(topicId);
        if (nextTopic != null) {
            return String.format("/courses/%d/%d_%d", courseId, nextTopic.getChapter().getSequence(), nextTopic.getSequence());
        }
        else {
            return String.format("/courses/%d", courseId);
        }
    }
}

