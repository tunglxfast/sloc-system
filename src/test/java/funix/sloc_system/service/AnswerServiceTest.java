package funix.sloc_system.service;

import funix.sloc_system.entity.Answer;
import funix.sloc_system.repository.AnswerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class AnswerServiceTest {

    @Autowired
    private AnswerService answerService;

    @Test
    public void testFindByQuestionIdAndCorrectTrue() {
        Long questionId = 1L;
        List<Answer> answers = answerService.findByQuestionIdAndCorrectTrue(questionId);
        assertNotNull(answers);
        assertFalse(answers.isEmpty());
        assertTrue(answers.size() == 1);
        assertTrue(answers.get(0).isCorrect() == true);
    }

    @Test
    public void testFindByQuestionIdAndCorrectTrue_NoAnswers() {
        Long questionId = 999L;
        List<Answer> answers = answerService.findByQuestionIdAndCorrectTrue(questionId);
        assertNotNull(answers);
        assertTrue(answers.isEmpty());
    }
} 