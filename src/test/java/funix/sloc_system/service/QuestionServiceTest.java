package funix.sloc_system.service;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.entity.Question;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.QuestionType;
import funix.sloc_system.repository.QuestionRepository;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.util.AppUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class QuestionServiceTest {

    @Autowired
    private QuestionService questionService;

    @Test
    public void testGetQuestionsByTopic() {
        Long topicId = 1L;
        List<Question> questions = new ArrayList<>();

        List<Question> result = questionService.getQuestionsByTopic(topicId);
        assertNotNull(result);
        assertEquals(questions, result);
    }

    @Test
    public void testSaveQuestionChanges_DraftCourse() throws Exception {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setId(1L);

        assertDoesNotThrow(() -> questionService.saveQuestionChanges(questionDTO, 1L));
    }

    @Test
    public void testSaveQuestionChanges_NonDraftCourse() throws Exception {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setId(1L);

        assertDoesNotThrow(() -> questionService.saveQuestionChanges(questionDTO, 1L));
    }

    @Test
    public void testHandleTopicQuestions_DraftCourse() throws Exception {
        Long topicId = 4L;
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setContent("1+1");
        questionDTO.setPoint(1);
        questionDTO.setTopicId(topicId);
        questionDTO.setQuestionType(QuestionType.INPUT_TEXT.name());

        questionDTOs.add(questionDTO);

        List<QuestionDTO> result = questionService.handleTopicQuestions(topicId, questionDTOs, 3L);
        assertNotNull(result);
        assertEquals(questionDTOs.size(), result.size());
    }

    @Test
    public void testCreateQuestion() throws Exception {
        Long topicId = 4L;
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setContent("1+1");
        questionDTO.setPoint(1);
        questionDTO.setTopicId(topicId);
        questionDTO.setQuestionType(QuestionType.INPUT_TEXT.name());
        QuestionDTO result = questionService.createQuestion(topicId, questionDTO, 3L);
        assertNotNull(result);
    }
} 