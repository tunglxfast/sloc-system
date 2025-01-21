package funix.sloc_system.service;

import funix.sloc_system.dto.TestResultDTO;
import funix.sloc_system.entity.*;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.QuestionType;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.repository.QuestionRepository;
import funix.sloc_system.repository.TestResultRepository;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class TestResultServiceTest {

    @Autowired
    private TestResultService testResultService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TestResultRepository testResultRepository;
    @Autowired
    private QuestionRepository questionRepository;

    private final long USER_ID = 4L;
    private final long TOPIC_ID = 5L;

    @BeforeEach
    public void setUp() {
      User user = userRepository.findById(USER_ID).orElse(null);
      Topic topic = topicRepository.findById(TOPIC_ID).orElse(null);
      TestResult testResult = new TestResult(0.0, 0.0, false, 1, TopicType.QUIZ.name(), user, topic);
      testResultRepository.save(testResult);
    }

    @Test
    public void testFindByUserIdAndTopicId() {
        TestResultDTO result = testResultService.findByUserIdAndTopicId(USER_ID, TOPIC_ID);
        assertNotNull(result);
    }

    @Test
    public void testCalculateScore() {
        Long userId = USER_ID;
        Long topicId = TOPIC_ID;
        Map<String, String> answers = new HashMap<>();
        answers.put("question_1", "Answer1");
        TestResultDTO result = testResultService.calculateScore(userId, topicId, answers);
        assertNotNull(result);
        assertFalse(result.getPassed());
    }

    @Test
    public void testCalculateScore_WrongUser() {
        Long userId = 999L;
        Long topicId = TOPIC_ID;
        Map<String, String> answers = new HashMap<>();
        answers.put("question_1", "Answer1");
        TestResultDTO result = testResultService.calculateScore(userId, topicId, answers);
        assertNull(result);
    }

    @Test
    public void testCalculateScore_NotTestTopic() {
        Long userId = USER_ID;
        Long topicId = 1L;
        Map<String, String> answers = new HashMap<>();
        answers.put("question_1", "Answer1");
        TestResultDTO result = testResultService.calculateScore(userId, topicId, answers);
        assertNull(result);
    }

    @Test
    public void testCalculateScore_InputTest() {
        Long userId = USER_ID;
        Long topicId = TOPIC_ID;
        // add input text
        Topic topic = topicRepository.findById(topicId).orElse(null);
        Question newQuestion = new Question();
        newQuestion.setContent("1+1");
        newQuestion.setQuestionType(QuestionType.INPUT_TEXT);
        newQuestion.setTopic(topic);
        newQuestion.setPoint(1);
        newQuestion.setContentStatus(ContentStatus.PUBLISHED);

        Answer answer = new Answer();
        answer.setQuestion(newQuestion);
        answer.setCorrect(true);
        answer.setContent("2");
        answer.setContentStatus(ContentStatus.PUBLISHED);
        newQuestion.addAnswer(answer);
        questionRepository.save(newQuestion);
        Long newQuestionId = newQuestion.getId();

        topic.setMaxPoint(topic.getMaxPoint() + 1);
        topicRepository.save(topic);

        Map<String, String> answers = new HashMap<>();
        answers.put("question_2", "A way to select HTML elements");
        answers.put("question_" + newQuestionId, "2");
        TestResultDTO result = testResultService.calculateScore(userId, topicId, answers);
        assertNotNull(result);
        assertTrue(result.getPassed());
        assertTrue(result.getHighestScore() == 100.0);
    }
} 