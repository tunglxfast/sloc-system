package funix.sloc_system.service;

import funix.sloc_system.dto.TestResultDTO;
import funix.sloc_system.entity.Answer;
import funix.sloc_system.entity.TestResult;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.repository.*;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.mapper.TestResultMapper;
import funix.sloc_system.mapper.TopicMapper;

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

    private final long USER_ID = 4L;
    private final long TOPIC_ID = 4L;

    @BeforeEach
    private void setUp() {
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
    }
} 