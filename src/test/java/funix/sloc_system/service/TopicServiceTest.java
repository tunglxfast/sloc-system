package funix.sloc_system.service;

import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.ContentChangeRepository;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.util.AppUtil;

import org.checkerframework.checker.units.qual.t;
import org.junit.jupiter.api.BeforeEach;
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
public class TopicServiceTest {

    @Autowired
    private TopicService topicService;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private CourseRepository courseRepository;

    private final long COURSE_ID = 1L;
    private final ApprovalStatus APP_STATUS = ApprovalStatus.NOT_SUBMITTED;
    private final ContentStatus CON_STATUS = ContentStatus.DRAFT;
    private final Long INSTRUCTOR_ID = 3L;

    @BeforeEach
    private void setUp() {
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
        Long topicId = 1L;
        Topic topic = topicService.findById(topicId);
        assertNotNull(topic);
    }

    @Test
    public void testFindByChapterId() {
        Long chapterId = 1L;
        List<Topic> topics = topicService.findByChapterId(chapterId);
        assertNotNull(topics);
        assertFalse(topics.isEmpty());
    }

    @Test
    public void testFindByChapterAndTopicSequence() {
        Long courseId = COURSE_ID; 
        int chapterSequence = 1;
        int topicSequence = 1;
        Topic topic = topicService.findByChapterAndTopicSequence(courseId, chapterSequence, topicSequence);
        assertNotNull(topic);
    }

    @Test
    public void testGetEditingTopicDTO() throws Exception {
        Long topicId = 1L; 
        TopicDTO topicDTO = topicService.getEditingTopicDTO(topicId);
        assertNotNull(topicDTO);
    }

    @Test
    public void testSaveTopicChanges() throws Exception {
        Long courseId = COURSE_ID;
        Long chapterId = 1L;
        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setId(1L); 
        topicDTO.setTitle("Updated Title");
        topicService.saveTopicChanges(courseId, chapterId, topicDTO, 1L); 
        Topic updatedTopic = topicService.findById(1L);
        assertEquals("Updated Title", updatedTopic.getTitle());
    }

    @Test
    public void testCreateTopic() throws Exception {
        Long chapterId = 1L;
        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setTitle("New Topic");
        topicDTO.setTopicType(TopicType.READING.name());
        topicDTO.setDescription("new");
        topicService.createTopic(chapterId, topicDTO, INSTRUCTOR_ID); 
        assertNotNull(topicDTO.getId());
    }

    @Test
    public void testDeleteTopic() throws Exception {
        Long courseId = COURSE_ID;
        Long chapterId = 1L;
        Long topicId = 1L;
        Topic topic = topicRepository.findById(topicId).orElse(null);
        assertNotNull(topic);
        topicService.deleteTopic(courseId, chapterId, topicId, INSTRUCTOR_ID); 
        assertNull(topicService.findById(topicId));
    }
} 