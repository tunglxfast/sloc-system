package funix.sloc_system.service;

import funix.sloc_system.dto.*;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.QuestionType;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.util.AppUtil;
import org.junit.jupiter.api.BeforeEach;
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
public class TopicServiceTest {

    @Autowired
    private TopicService topicService;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AppUtil appUtil;

    private final long COURSE_ID = 1L;
    private final long CHAPTER_ID = 1L;
    private final long TOPIC_ID = 1L;
    private final long QUIZ_ID = 4L;

    private final ApprovalStatus APP_STATUS = ApprovalStatus.NOT_SUBMITTED;
    private final ContentStatus CON_STATUS = ContentStatus.DRAFT;
    private final Long INSTRUCTOR_ID = 3L;

    @BeforeEach
    public void setUp() {
      //make course 1L draft
      Course course = courseRepository.findById(COURSE_ID).orElse(null);
      course.setApprovalStatus(APP_STATUS);
      course.setContentStatus(CON_STATUS);
      for (Chapter chapter : course.getChapters()) {
        chapter.setContentStatus(CON_STATUS);
        for (Topic topic : chapter.getTopics()) {
          topic.setContentStatus(CON_STATUS);
        }
      }
    }

    @Test
    public void testFindById() {
        Topic topic = topicService.findById(TOPIC_ID);
        assertNotNull(topic);
    }

    @Test
    public void testFindByChapterId() {
        List<Topic> topics = topicService.findByChapterId(CHAPTER_ID);
        assertNotNull(topics);
        assertFalse(topics.isEmpty());
    }

    @Test
    public void testFindByChapterAndTopicSequence() {
        int chapterSequence = 1;
        int topicSequence = 1;
        Topic topic = topicService.findByChapterAndTopicSequence(COURSE_ID, chapterSequence, topicSequence);
        assertNotNull(topic);
    }

    @Test
    public void testGetEditingTopicDTO() throws Exception {
        TopicDTO topicDTO = topicService.getEditingTopicDTO(TOPIC_ID);
        assertNotNull(topicDTO);
    }

    @Test
    public void testSaveTopicChanges() throws Exception {
        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setId(TOPIC_ID);
        topicDTO.setTitle("Updated Title");
        topicDTO.setDescription("New topic");
        topicDTO.setSequence(999);
        topicDTO.setFileUrl("/reading/");
        topicDTO.setVideoUrl("/youtube/");

        topicService.saveTopicChanges(COURSE_ID, CHAPTER_ID, topicDTO, INSTRUCTOR_ID);
        Topic updatedTopic = topicService.findById(TOPIC_ID);
        assertEquals("Updated Title", updatedTopic.getTitle());
    }

    @Test
    public void testSaveTopicChanges_TestTopic() throws Exception {
        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setId(QUIZ_ID);
        topicDTO.setTitle("Updated Title");
        topicDTO.setDescription("New topic");
        topicDTO.setSequence(999);
        topicDTO.setQuestions(new ArrayList<>());
        topicDTO.addQuestion(new QuestionDTO());
        topicDTO.setPassPoint(1);
        topicDTO.setMaxPoint(1);
        topicDTO.setTimeLimit(10);

        topicService.saveTopicChanges(COURSE_ID, CHAPTER_ID, topicDTO, INSTRUCTOR_ID);
        Topic updatedTopic = topicService.findById(QUIZ_ID);
        assertEquals("Updated Title", updatedTopic.getTitle());
    }

    @Test
    public void testCreateTopic() throws Exception {
        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setTitle("New Topic");
        topicDTO.setTopicType(TopicType.READING.name());
        topicDTO.setDescription("new");
        topicService.createTopic(CHAPTER_ID, topicDTO, INSTRUCTOR_ID);
        assertNotNull(topicDTO.getId());
    }

    @Test
    public void testCreateTopic_PublishedCourse() throws Exception {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        assertNotNull(course);
        course.setContentStatus(ContentStatus.PUBLISHED);
        course.setApprovalStatus(ApprovalStatus.APPROVED);
        courseRepository.save(course);

        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setTitle("New Topic");
        topicDTO.setTopicType(TopicType.QUIZ.name());
        topicDTO.setDescription("new");

        // add input text
        QuestionDTO newQuestion = new QuestionDTO();
        newQuestion.setContent("1+1");
        newQuestion.setQuestionType(QuestionType.INPUT_TEXT.name());
        newQuestion.setPoint(1);
        newQuestion.setContentStatus(ContentStatus.PUBLISHED.name());
        topicDTO.setMaxPoint(1);
        topicDTO.addQuestion(newQuestion);

        AnswerDTO answer = new AnswerDTO();
        answer.setCorrect(true);
        answer.setContent("2");
        answer.setContentStatus(ContentStatus.PUBLISHED.name());
        newQuestion.addAnswer(answer);

        topicService.createTopic(CHAPTER_ID, topicDTO, INSTRUCTOR_ID);
        assertNotNull(topicDTO.getId());
    }

    @Test
    public void testDeleteTopic() throws Exception {
        Topic topic = topicRepository.findById(TOPIC_ID).orElse(null);
        assertNotNull(topic);
        topicService.deleteTopic(COURSE_ID, CHAPTER_ID, TOPIC_ID, INSTRUCTOR_ID);
        assertNull(topicService.findById(TOPIC_ID));
    }

    @Test
    public void testDeleteTopic_PublishedCourse() throws Exception {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        assertNotNull(course);
        course.setContentStatus(ContentStatus.PUBLISHED);
        course.setApprovalStatus(ApprovalStatus.APPROVED);
        courseRepository.save(course);

        Topic topic = topicRepository.findById(TOPIC_ID).orElse(null);
        assertNotNull(topic);
        topicService.deleteTopic(COURSE_ID, CHAPTER_ID, TOPIC_ID, INSTRUCTOR_ID);
        assertNotNull(topicService.findById(TOPIC_ID));

        CourseDTO courseDTO = appUtil.getEditingCourseDTO(COURSE_ID);
        boolean isTopicDelete = true;
        for (ChapterDTO c : courseDTO.getChapters()) {
            if (!isTopicDelete) {
                break;
            }
            for (TopicDTO t : c.getTopics()) {
                if (t.getId().equals(TOPIC_ID)) {
                    isTopicDelete = false;
                    break;
                }
            }
        }
        assertTrue(isTopicDelete);
    }
} 