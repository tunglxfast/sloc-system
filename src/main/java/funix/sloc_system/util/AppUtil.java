package funix.sloc_system.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.TestResultDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.ContentChangeTemporary;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.*;
import funix.sloc_system.mapper.*;
import funix.sloc_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
    private TestResultRepository testResultRepository;
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
    @Autowired
    private TestResultMapper testResultMapper;

    private static final String FINAL_SCORE = "finalScore";
    private static final String IS_PASSED = "isPassed";

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
    public void saveContentChange(String json, Long entityId, Long instructorId, ContentAction action) {
        ContentChangeTemporary changeTemporary = contentChangeRepository
                .findByEntityTypeAndEntityId(EntityType.COURSE, entityId)
                .orElse(new ContentChangeTemporary());

        changeTemporary.setEntityType(EntityType.COURSE);
        changeTemporary.setEntityId(entityId);
        changeTemporary.setAction(action);
        changeTemporary.setChanges(json);
        changeTemporary.setChangeTime(LocalDateTime.now());

        changeTemporary.setUpdatedBy(instructorId);

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
            if (chapterDTO.getId() == null) {
                continue;
            }
            if (chapterDTO.getId().equals(chapterId)) {
                return chapterDTO;
            }
        }
        // throw exeption if not found any match with chapterId
        throw new IllegalArgumentException(String.format("Chapter with id: %d not found", chapterId));
    }

    public static TopicDTO getSelectTopicDTO(ChapterDTO chapterDTO, Long topicId) {
        for (TopicDTO topicDTO : chapterDTO.getTopics()) {
            if (topicDTO.getId() == null) {
                continue;
            }
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

    /**
     * Reorder chapters sequence
     */
    public static void reorderCourseDTOChapters(CourseDTO courseDTO) {
        List<ChapterDTO> chapters = courseDTO.getChapters();
        chapters.sort(Comparator.comparingInt(ChapterDTO::getSequence));

        for (int i = 0; i < chapters.size(); i++) {
            chapters.get(i).setSequence(i + 1);
        }
    }

    public static void reorderChapterDTOTopics(ChapterDTO chapterDTO) {
        List<TopicDTO> topics = chapterDTO.getTopics();
        topics.sort(Comparator.comparingInt(TopicDTO::getSequence));

        for (int i = 0; i < topics.size(); i++) {
            topics.get(i).setSequence(i + 1);
        }
    }

    public boolean checkCourseIsEditing(Long courseId) throws Exception {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Editing course not found"));

        ContentStatus contentStatus = course.getContentStatus();
        if (contentStatus == ContentStatus.DRAFT) {
            return true;
        }
        ContentChangeTemporary changeTemporary = contentChangeRepository
            .findByEntityTypeAndEntityId(EntityType.COURSE, courseId).orElse(null);

        return changeTemporary != null;
    }

    public CourseEditingHolder getCourseEditingHolder(Long courseId) throws Exception {
        CourseDTO courseDTO = getEditingCourseDTO(courseId);
        boolean isEditing = checkCourseIsEditing(courseId);
        boolean isPending = courseDTO.getApprovalStatus().equals(ApprovalStatus.PENDING.name());
        return new CourseEditingHolder(courseDTO, isEditing, isPending);
    }

    public List<CourseEditingHolder> getCourseEditingHolders(List<CourseDTO> courses) {
        List<CourseEditingHolder> courseEditingHolders = new ArrayList<>();
        boolean isEditing = false;
        boolean isPending = false;
        for (CourseDTO courseDTO : courses) {
            try {
                isEditing = checkCourseIsEditing(courseDTO.getId());
                isPending = courseDTO.getApprovalStatus().equals(ApprovalStatus.PENDING.name());
                courseEditingHolders.add(new CourseEditingHolder(courseDTO, isEditing, isPending));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return courseEditingHolders;
    }

    /**
     * Get all course marks and calculate final score
     * @param userId
     * @param courseId
     * @return Map of values: 'topicResult' - List<TestResultDTO>,
     * 'finalScore' - Integer of final score (nullable),
     * 'isPassed' - Boolean define pass the course or not (nullable).
     */
    @Transactional
    public Map<String, Object> calculateCoursePoint(Long userId, Long courseId) {
        Map<String, Object> result = new HashMap<>();
        boolean isCalculateFinal = true;
        Course course = courseRepository.findById(courseId).orElse(null);
        List<TestResultDTO> testResults = new ArrayList<>();
        if (course == null) {
            result.put("topicResult", testResults);
            result.put(FINAL_SCORE, null);
            result.put(IS_PASSED, null);
            return result;
        }

        CourseDTO courseDTO = courseMapper.toDTO(course);
        List<TopicDTO> haveTestTopic = new ArrayList<>();

        String topicType;
        for (ChapterDTO chapter : courseDTO.getChapters()) {
            for (TopicDTO topic : chapter.getTopics()) {
                topicType = topic.getTopicType();
                if (topicType.equalsIgnoreCase(TopicType.EXAM.name())
                        || topicType.equalsIgnoreCase(TopicType.QUIZ.name())) {
                    haveTestTopic.add(topic);
                }
            }
        }

        TestResultDTO testResultDTO;
        for (TopicDTO topic : haveTestTopic) {
            testResultDTO = testResultMapper.toDTO(
                    testResultRepository
                            .findByUserIdAndTopicId(userId, topic.getId())
                            .orElse(null)
            );

            if (testResultDTO == null) {
                testResultDTO = new TestResultDTO();
                testResultDTO.setTopic(topic);
                testResultDTO.setTestType(topic.getTopicType());
                // when student haven't finished all quizzes and exams, do not calculate final score
                isCalculateFinal = false;
            }
            testResults.add(testResultDTO);
        }

        // set attributes
        result.put("topicResult", testResults);

        if (isCalculateFinal) {
            Map<String, Object> finalCourseResult = calculateFinalScore(testResults);
            result.put(FINAL_SCORE, finalCourseResult.get(FINAL_SCORE));
            result.put(IS_PASSED, finalCourseResult.get(IS_PASSED));
        } else {
            result.put(FINAL_SCORE, null);
            result.put(IS_PASSED, null);
        }

        return result;
    }

    private Map<String, Object> calculateFinalScore(List<TestResultDTO> testResults) {
        // Final score = quiz * 0.4 + exam * 0.6
        Double quizzesScore = 0.0;
        Double examsScore = 0.0;
        boolean allTestPass = true;
        boolean passed;

        for (TestResultDTO testResultDTO : testResults) {
            if (testResultDTO.getTestType().equalsIgnoreCase(TopicType.QUIZ.name())) {
                if (testResultDTO.getHighestScore() < 50.0) {
                    allTestPass = false;
                }
                quizzesScore += testResultDTO.getHighestScore();
            } else if (testResultDTO.getTestType().equalsIgnoreCase(TopicType.EXAM.name())) {
                if (testResultDTO.getHighestScore() < 50.0) {
                    allTestPass = false;
                }
                examsScore  += testResultDTO.getHighestScore();
            }
        }

        int calculatedScore = (int)Math.round((quizzesScore * 0.4) + (examsScore * 0.6));

        if (calculatedScore >= 50 && allTestPass) {
            passed = true;
        } else {
            passed = false;
        }
        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put(FINAL_SCORE, calculatedScore);
        finalResult.put(IS_PASSED, passed);
        return finalResult;
    }
}

