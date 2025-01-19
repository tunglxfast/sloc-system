package funix.sloc_system.service;

import funix.sloc_system.entity.ScoreWeight;
import funix.sloc_system.repository.ScoreWeightRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.doubleThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class ScoreWeightServiceTest {

    @Autowired
    private ScoreWeightService scoreWeightService;

    @Autowired
    private ScoreWeightRepository scoreWeightRepository;

    @Test
    public void testGetScoreWeightByCourseId() {
        Long courseId = 1L;
        double examWeight = 0.5;
        double quizWeight = 0.5;
        ScoreWeight scoreWeight = new ScoreWeight();
        scoreWeight.setExamWeight(examWeight);
        scoreWeight.setQuizWeight(quizWeight);
        scoreWeight.setCourseId(courseId);
        scoreWeightRepository.save(scoreWeight);

        ScoreWeight result = scoreWeightService.getScoreWeightByCourseId(courseId);
        assertNotNull(result);
        assertEquals(quizWeight, result.getQuizWeight());
        assertEquals(examWeight, result.getExamWeight());
    }

    @Test
    public void testSaveScoreWeight() {
        Long courseId = 1L;
        double quizWeight = 0.4;
        double examWeight = 0.6;

        ScoreWeight result = scoreWeightService.saveScoreWeight(courseId, quizWeight, examWeight);
        assertNotNull(result);
        assertEquals(quizWeight, result.getQuizWeight());
        assertEquals(examWeight, result.getExamWeight());
    }

    @Test
    public void testSaveDefaultScoreWeight() {
        Long courseId = 1L;

        ScoreWeight result = scoreWeightService.saveDefaultScoreWeight(courseId);
        assertNotNull(result);
        assertEquals(ScoreWeightService.DEFAULT_QUIZ_WEIGHT, result.getQuizWeight());
        assertEquals(ScoreWeightService.DEFAULT_EXAM_WEIGHT, result.getExamWeight());
    }
} 