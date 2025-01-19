package funix.sloc_system.service;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.AnswerDTO;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class DTOServiceTest {

    @InjectMocks
    private DTOService dtoService;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseMapper courseMapper;

    public DTOServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAvailableChapters() {
        CourseDTO courseDTO = new CourseDTO();
        ChapterDTO chapterDTO = new ChapterDTO();
        chapterDTO.setContentStatus(ContentStatus.PUBLISHED.name());
        List<ChapterDTO> chapters = new ArrayList<>();
        chapters.add(chapterDTO);
        courseDTO.setChapters(chapters);
        List<ChapterDTO> availableChapters = dtoService.getAvailableChapters(courseDTO);
        assertEquals(1, availableChapters.size());
    }

    @Test
    public void testGetAvailableTopics() {
        ChapterDTO chapterDTO = new ChapterDTO();
        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setContentStatus(ContentStatus.PUBLISHED.name());
        List<TopicDTO> topics = new ArrayList<>();
        topics.add(topicDTO);
        chapterDTO.setTopics(topics);
        List<TopicDTO> availableTopics = dtoService.getAvailableTopics(chapterDTO);
        assertEquals(1, availableTopics.size());
    }

    @Test
    public void testGetAvailableQuestions() {
        TopicDTO topicDTO = new TopicDTO();
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setContentStatus(ContentStatus.PUBLISHED.name());
        List<QuestionDTO> questions = new ArrayList<>();
        questions.add(questionDTO);
        topicDTO.setQuestions(questions);
        List<QuestionDTO> availableQuestions = dtoService.getAvailableQuestions(topicDTO);
        assertEquals(1, availableQuestions.size());
    }

    @Test
    public void testGetAvailableAnswers() {
        QuestionDTO questionDTO = new QuestionDTO();
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setContentStatus(ContentStatus.PUBLISHED.name());
        List<AnswerDTO> answers = new ArrayList<>();
        answers.add(answerDTO);
        questionDTO.setAnswers(answers);
        List<AnswerDTO> availableAnswers = dtoService.getAvailableAnswers(questionDTO);
        assertEquals(1, availableAnswers.size());
    }
} 