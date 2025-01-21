package funix.sloc_system.service;

import funix.sloc_system.entity.*;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.TestResultRepository;
import funix.sloc_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class StudyProcessServiceTest {

    @Autowired
    private StudyProcessService studyProcessService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    private final Long COURSE_ID = 1L;
    private final Long STUDENT_ID = 4L;
    private final Long TOPIC_ID = 4L;

    @Test
    public void testFindByUserIdAndCourseId() {
        Long userId = STUDENT_ID;
        Long courseId = COURSE_ID;
        StudyProcess studyProcess = studyProcessService.findByUserIdAndCourseId(userId, courseId);
        assertNotNull(studyProcess);
    }

    @Test
    public void testSaveLastViewTopic() {
        StudyProcess studyProcess = studyProcessService.saveLastViewTopic(STUDENT_ID, COURSE_ID, TOPIC_ID);
        assertNotNull(studyProcess);
        assertEquals(TOPIC_ID, studyProcess.getLastViewTopic());
    }

    @Test
    public void testSaveLastViewTopic_ReadingTopic() {
        Long readingTopicId = 1L;
        StudyProcess studyProcess = studyProcessService.saveLastViewTopic(STUDENT_ID, COURSE_ID, readingTopicId);
        assertNotNull(studyProcess);
        assertEquals(readingTopicId, studyProcess.getLastViewTopic());
    }

    @Test
    public void testSaveLastViewTopic_UserIdNull() {
        StudyProcess studyProcess = studyProcessService.saveLastViewTopic(null, COURSE_ID, TOPIC_ID);
        assertNull(studyProcess);
    }

    @Test
    public void testCalculateAndSaveFinalResult_NotLearning() {
        String result = studyProcessService.calculateAndSaveFinalResult(5L, COURSE_ID, TOPIC_ID);
        assertEquals("User didn't learning anything yet.", result);
    }

    @Test
    public void testCalculateAndSaveFinalResult_HaveTestPoint() {
        // add test point
        User user = userRepository.findById(STUDENT_ID).orElse(null);
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        for (Chapter chapter : course.getChapters()) {
            for (Topic topic : chapter.getTopics()) {
                if (topic.getTopicType().equals(TopicType.QUIZ)
                        || topic.getTopicType().equals(TopicType.EXAM)) {
                    TestResult testResult = new TestResult();
                    testResult.setTestType(topic.getTopicType().name());
                    testResult.setHighestScore(100.0);
                    testResult.setLatestScore(100.0);
                    testResult.setPassed(true);
                    testResult.setUser(user);
                    testResult.setTopic(topic);
                    testResultRepository.save(testResult);
                }
            }
        }

        String result = studyProcessService.calculateAndSaveFinalResult(STUDENT_ID, COURSE_ID, TOPIC_ID);
        assertEquals("Calculate and save score successfully.", result);
    }

    @Test
    public void testCalculateAndSaveFinalResult_NotCalculateFinal() {
        // add test point
        User user = userRepository.findById(STUDENT_ID).orElse(null);
        Course course = courseRepository.findById(COURSE_ID).orElse(null);

        boolean isBreak = false;
        for (Chapter chapter : course.getChapters()) {
            if (isBreak) {
                break;
            }
            for (Topic topic : chapter.getTopics()) {
                if (isBreak) {
                    break;
                }
                if (topic.getTopicType().equals(TopicType.QUIZ)
                        || topic.getTopicType().equals(TopicType.EXAM)) {
                    TestResult testResult = new TestResult();
                    testResult.setTestType(topic.getTopicType().name());
                    testResult.setHighestScore(100.0);
                    testResult.setLatestScore(100.0);
                    testResult.setPassed(true);
                    testResult.setUser(user);
                    testResult.setTopic(topic);
                    testResultRepository.save(testResult);
                    isBreak = true;
                }
            }
        }

        String result = studyProcessService.calculateAndSaveFinalResult(STUDENT_ID, COURSE_ID, TOPIC_ID);
        assertEquals("Not qualify to calculate final score yet.", result);
    }
} 