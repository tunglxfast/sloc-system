package funix.sloc_system.service;

import funix.sloc_system.entity.*;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.RankingRepository;
import funix.sloc_system.repository.TestResultRepository;
import funix.sloc_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class RankingServiceTest {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RankingRepository rankingRepository;

    @Autowired

    private TestResultRepository testResultRepository;

    private final Long USER_1_ID = 4L;
    private final Long USER_2_ID = 5L;
    private final Long COURSE_ID = 1L;

    @BeforeEach
    public void setUp() {
        // prepared data
        User user_1 = userRepository.findById(USER_1_ID).orElse(null);
        User user_2 = userRepository.findById(USER_2_ID).orElse(null);
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        Topic topic = course.getChapters().get(0).getTopics().get(1);
        TestResult testResult_1 = new TestResult();
        testResult_1.setTestType(TopicType.QUIZ.name());
        testResult_1.setHighestScore(10.0);
        testResult_1.setLatestScore(10.0);
        testResult_1.setPassed(false);
        testResult_1.setUser(user_1);
        testResult_1.setTopic(topic);
        testResultRepository.save(testResult_1);

        TestResult testResult_2 = new TestResult();
        testResult_2.setTestType(TopicType.QUIZ.name());
        testResult_2.setHighestScore(5.0);
        testResult_2.setLatestScore(5.0);
        testResult_2.setPassed(false);
        testResult_2.setUser(user_2);
        testResult_2.setTopic(topic);
        testResultRepository.save(testResult_2);
    }

    @Test
    public void testUpdateRankings() {
        assertDoesNotThrow(() -> rankingService.updateRankings());
    }

    @Test
    public void testCalculateAndSaveRankings() {
        Long courseId = COURSE_ID;
        List<Ranking> result = rankingService.calculateAndSaveRankings(courseId);
        assertNotNull(result);
    }

    @Test
    public void testGetRankingsByCourse() {
        Long courseId = COURSE_ID;
        List<Ranking> rankings = rankingService.getRankingsByCourse(courseId);
        assertNotNull(rankings);
    }

    @Test
    public void testGetRankingsByUserAndCourse() {
        Long userId = USER_1_ID;
        Long courseId = COURSE_ID;
        rankingService.calculateAndSaveRankings(courseId);
        Ranking result = rankingService.getRankingsByUserAndCourse(userId, courseId);
        assertNotNull(result);
    }

    @Test
    public void testGetRankingsByUser() {
        Long userId = USER_1_ID;
        List<Ranking> rankings = rankingService.getRankingsByUser(userId);
        assertNotNull(rankings);
    }
} 