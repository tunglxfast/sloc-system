package funix.sloc_system.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import funix.sloc_system.dto.*;
import funix.sloc_system.entity.*;
import funix.sloc_system.enums.*;
import funix.sloc_system.mapper.*;
import funix.sloc_system.repository.*;
import funix.sloc_system.service.ScoreWeightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private StudyProcessRepository studyProcessRepository;
    @Autowired
    private LearnedTopicRepository learnedTopicRepository;
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
    @Autowired
    private ScoreWeightService scoreWeightService;

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

    public String getPreviousTopicUrl(Long topicId, Long courseId) {
        Topic topic = topicRepository.findById(topicId).orElse(null);
        if (topic != null) {
            Chapter currentChapter = topic.getChapter();
            int targetTopicSeq = 0;
            int targetChapterSeq = 0;

            if (topic.getSequence() > 1) {
                targetTopicSeq = topic.getSequence() - 1;
                targetChapterSeq = topic.getChapter().getSequence();
                return String.format("/courses/%d/%d_%d", courseId, targetChapterSeq, targetTopicSeq);

            } else if (currentChapter.getSequence() > 1) {
                targetChapterSeq = currentChapter.getSequence()-1;
                Course currentCourse = currentChapter.getCourse();
                List<Chapter> chapters = chapterRepository.findByCourseIdOrderBySequence(currentCourse.getId());
                for (Chapter chapter: chapters) {
                    if (chapter.getSequence() == targetChapterSeq) {
                        List<Topic> topics = chapter.getTopics();
                        targetTopicSeq = topics.get(topics.size() - 1).getSequence();
                        return String.format("/courses/%d/%d_%d", courseId, targetChapterSeq, targetTopicSeq);
                    }
                }
            } else {
                return String.format("/courses/%d", courseId);
            }            
        }

        return String.format("/courses/%d", courseId);
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

    @Transactional
    public List<TestResultDTO> getCourseTestsResult(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return new ArrayList<>();
        }

        List<TestResultDTO> testResults = new ArrayList<>();
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
                // add none score testResult to testResults
                testResultDTO = new TestResultDTO();
                testResultDTO.setTopic(topic);
                testResultDTO.setTestType(topic.getTopicType());
            }
            testResults.add(testResultDTO);
        }

        return testResults;
    }

    public Map<String, Object> calculateFinalScore(List<TestResultDTO> testResults, Long courseId) {
        ScoreWeight scoreWeight = scoreWeightService.getScoreWeightByCourseId(courseId);
        double quizWeight = ScoreWeightService.DEFAULT_QUIZ_WEIGHT;
        double examWeight = ScoreWeightService.DEFAULT_EXAM_WEIGHT;
        if (scoreWeight != null) {
            quizWeight = scoreWeight.getQuizWeight();
            examWeight = scoreWeight.getExamWeight();
        }

        Double quizzesScore = 0.0;
        Double examsScore = 0.0;
        int calculatedScore;
        int testsCount = testResults.size();
        boolean allTestPass = true;
        boolean isAllQuiz = true;
        boolean isAllExam = true;

        for (TestResultDTO testResultDTO : testResults) {
            if (testResultDTO.getTestType().equalsIgnoreCase(TopicType.QUIZ.name())) {
                isAllExam = false;

                if (testResultDTO.getPassed() == null || !testResultDTO.getPassed()) {
                    allTestPass = false;
                }
                quizzesScore += testResultDTO.getHighestScore();
            } else if (testResultDTO.getTestType().equalsIgnoreCase(TopicType.EXAM.name())) {
                isAllQuiz = false;

                if (testResultDTO.getPassed() == null || !testResultDTO.getPassed()) {
                    allTestPass = false;
                }
                examsScore  += testResultDTO.getHighestScore();
            }
        }


        if (isAllQuiz) {
            calculatedScore = (int)Math.round(quizzesScore / testsCount);
        } else if (isAllExam) {
            calculatedScore = (int)Math.round(examsScore / testsCount);
        } else {
            calculatedScore = (int)Math.round(((quizzesScore * quizWeight) + (examsScore * examWeight))/testsCount);
        }

        boolean passed = calculatedScore >= 50 && allTestPass;
        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put(FINAL_SCORE, calculatedScore);
        finalResult.put(IS_PASSED, passed);
        return finalResult;
    }

    @Transactional
    public void evaluateStudentsStudy(Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return;
        }
        CourseDTO courseDTO = courseMapper.toDTO(course);
        // count have test topic
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

        int testCount = haveTestTopic.size();
        if (testCount == 0) {
            return;
        }

        // get student enroll
        Set<Long> studentEnrollId = new HashSet<>();
        Set<EnrollmentDTO> enrollmentDTOList = courseDTO.getEnrollments();

        for (EnrollmentDTO enrollmentDTO : enrollmentDTOList) {
            studentEnrollId.add(enrollmentDTO.getUser().getId());
        }

        if (studentEnrollId.isEmpty()) {
            return;
        }

        long passDay = LocalDate.now().toEpochDay() - courseDTO.getStartDate().toEpochDay();
        long totalDay = courseDTO.getEndDate().toEpochDay() - courseDTO.getStartDate().toEpochDay();
        double dayPassRatio = ((double) passDay)/totalDay;

        // begin evaluate each student
        int testPassCount;
        double testPassRatio;
        int testPassNeeded;
        StudyProcess studyProcess;
        for (Long studentId : studentEnrollId) {
            studyProcess = studyProcessRepository.findByUserIdAndCourseId(studentId, courseDTO.getId()).orElse(null);
            if (studyProcess == null) {
                studyProcess = new StudyProcess();
                studyProcess.setUserId(studentId);
                studyProcess.setCourseId(courseId);
                studyProcess.setProgressAssessment("You should begin studying.");
                studyProcessRepository.save(studyProcess);
                return;
            }

            if (studyProcess.getLearningProgress() < dayPassRatio) {
                studyProcess.setProgressAssessment("You are falling behind in your studies, try harder.");
                studyProcessRepository.save(studyProcess);
                return;
            }

            testPassCount = 0;
            for (TopicDTO topicDTO : haveTestTopic) {
                TestResult testResult = testResultRepository.findByUserIdAndTopicId(studentId, topicDTO.getId()).orElse(null);
                if (testResult != null && Boolean.TRUE.equals(testResult.getPassed())) {
                    testPassCount += 1;
                }
            }
            testPassRatio = ((double) testPassCount)/testCount;
            if (testPassRatio < dayPassRatio) {
                testPassNeeded = (int) Math.round(testCount*dayPassRatio);
                studyProcess.setProgressAssessment(String.format("You are behind schedule. " +
                                "You currently have to complete at least %d" +
                                " exercises/tests but have only completed %d.",
                        testPassNeeded, testPassCount));
            } else {
                studyProcess.setProgressAssessment("Very good, you have done well in your studies, keep up the good work.");
            }
            studyProcessRepository.save(studyProcess);
        }
    }

    /**
     * Percent of learned topics / total topics
     * @param userId
     * @param courseId
     * @return
     */
    @Transactional
    public double calculateLearningProgress(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return 0.0;
        }
        Set<Topic> topics = new HashSet<>();
        for (Chapter chapter : course.getChapters()) {
            if (chapter.getTopics() != null) {
                topics.addAll(chapter.getTopics());
            }
        }

        Set<Topic> learnedTopics = new HashSet<>();

        for (Topic topic : topics) {
            if (learnedTopicRepository.existsByUserIdAndTopicId(userId, topic.getId())) {
                learnedTopics.add(topic);
            }
        }

        return (((double) learnedTopics.size())/topics.size()) * 100;
    }

    /**
     * Remove unnecessary topics from courseDTO.
     * @param courseDTO
     */
    public CourseDTO removeUnnecessaryTopicTypes(CourseDTO courseDTO, List<String> topicTypes) {
        if (courseDTO == null || topicTypes == null || topicTypes.isEmpty()) {
            return null;
        }
        
        List<ChapterDTO> newChapters = new ArrayList<>();
        List<TopicDTO> newTopics;
        for (ChapterDTO chapter : courseDTO.getChapters()) {
            newTopics = new ArrayList<>();
            for (TopicDTO topic : chapter.getTopics()) {
                if (!topicTypes.contains(topic.getTopicType())) {
                    newTopics.add(topic);
                }
            }
            chapter.setTopics(newTopics);
            newChapters.add(chapter);
        }
        courseDTO.setChapters(newChapters);
        return courseDTO;
    }

        /* 
     * Check if user has access to this course.
     * 
     * @param courseId the id of the course
     * @param userId the id of the user
     * @return true if user has access to this course, false otherwise
     */
    public boolean checkCourseAccessAbility(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return false;
        }

        boolean isInstructor = course.getInstructor().getId().equals(userId);
        if (isInstructor) {
            return true;
        }

        boolean isEnrolled = course.getEnrollments().stream().anyMatch(enrollment -> enrollment.getUser().getId().equals(userId));
        if (isEnrolled) {
            return true;
        }
        return false;
    }   

    /**
     * Check if user has access to this topic.
     * @param topicId the id of the topic
     * @param userId the id of the user
     * @return true if user has access to this topic, false otherwise
     */
    public boolean checkCourseAccessAbilityByTopicId(Long topicId, Long userId) {
        Topic topic = topicRepository.findById(topicId).orElse(null);
        if (topic == null) {
            return false;
        }
        Long courseId = topic.getChapter().getCourse().getId();
        return checkCourseAccessAbility(courseId, userId);
    }
}

