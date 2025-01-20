package funix.sloc_system.service;

import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Question;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.QuestionType;
import funix.sloc_system.mapper.QuestionMapper;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.QuestionRepository;
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
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private AppUtil appUtil;

    private final Long COURSE_ID = 1L;
    private final Long TOPIC_ID = 4L;
    private final Long INSTRUCTOR_ID = 3L;
    private final String CONTENT = "1+1";
    private final Long EDIT_QUESTION_ID = 1L;


    @Test
    public void testGetQuestionsByTopic() {
        Long topicId = TOPIC_ID;

        List<Question> result = questionService.getQuestionsByTopic(topicId);
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    public void testSaveQuestionChanges_DraftCourse() throws Exception {
        Question editQuestion = questionRepository.findById(EDIT_QUESTION_ID).orElse(null);
        String oldContent = editQuestion.getContent();

        QuestionDTO questionDTO = questionMapper.toDTO(editQuestion);
        questionDTO.setContent(CONTENT);

        // make course draft
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        assertNotNull(course);
        course.setContentStatus(ContentStatus.DRAFT);
        course.setApprovalStatus(ApprovalStatus.NOT_SUBMITTED);
        courseRepository.save(course);

        assertDoesNotThrow(() -> questionService.saveQuestionChanges(questionDTO, INSTRUCTOR_ID));
        Question question = questionRepository.findById(EDIT_QUESTION_ID).orElse(null);
        assertNotNull(question);
        assertNotEquals(oldContent, question.getContent());
        assertEquals(CONTENT, question.getContent());
    }

    @Test
    public void testSaveQuestionChanges_NonDraftCourse() throws Exception {
        Question editQuestion = questionRepository.findById(EDIT_QUESTION_ID).orElse(null);
        String oldContent = editQuestion.getContent();

        QuestionDTO questionDTO = questionMapper.toDTO(editQuestion);
        questionDTO.setContent(CONTENT);

        assertDoesNotThrow(() -> questionService.saveQuestionChanges(questionDTO, INSTRUCTOR_ID));

        Question question = questionRepository.findById(EDIT_QUESTION_ID).orElse(null);
        assertNotNull(question);
        assertEquals(oldContent, question.getContent());

        CourseDTO course = appUtil.getEditingCourseDTO(COURSE_ID);
        boolean updated = false;
        for (ChapterDTO chapterDTO : course.getChapters()) {
            for (TopicDTO topicDTO : chapterDTO.getTopics()) {
                if (topicDTO.getQuestions()!=null) {
                    for (QuestionDTO q : topicDTO.getQuestions()) {
                        if (EDIT_QUESTION_ID.equals(q.getId())) {
                            if (CONTENT.equals(q.getContent())){
                                updated = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        assertTrue(updated);
    }

    @Test
    public void testHandleTopicQuestions_DraftCourse() throws Exception {
        // make course draft
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        assertNotNull(course);
        course.setContentStatus(ContentStatus.DRAFT);
        course.setApprovalStatus(ApprovalStatus.NOT_SUBMITTED);
        courseRepository.save(course);

        Long topicId = TOPIC_ID;
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        Question question = questionRepository.findById(EDIT_QUESTION_ID).orElse(null);
        QuestionDTO questionDTO = questionMapper.toDTO(question);
        questionDTO.setContent(CONTENT);
        questionDTOs.add(questionDTO);

        List<QuestionDTO> result = questionService.handleTopicQuestions(topicId, questionDTOs, INSTRUCTOR_ID);
        assertNotNull(result);
        assertEquals(questionDTOs.size(), result.size());
    }

    @Test
    public void testCreateQuestion() throws Exception {
        Long topicId = TOPIC_ID;
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setContent(CONTENT);
        questionDTO.setPoint(1);
        questionDTO.setTopicId(topicId);
        questionDTO.setQuestionType(QuestionType.INPUT_TEXT.name());
        QuestionDTO result = questionService.createQuestion(topicId, questionDTO, INSTRUCTOR_ID);
        assertNotNull(result);
    }
} 