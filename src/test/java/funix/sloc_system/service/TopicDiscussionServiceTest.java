package funix.sloc_system.service;

import funix.sloc_system.dto.TopicDiscussionDTO;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.entity.TopicDiscussion;
import funix.sloc_system.entity.User;
import funix.sloc_system.repository.TopicDiscussionRepository;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import funix.sloc_system.mapper.TopicDiscussionMapper;
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
public class TopicDiscussionServiceTest {

    @Autowired
    private TopicDiscussionService topicDiscussionService;

    @Autowired
    private TopicDiscussionRepository topicDiscussionRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicDiscussionMapper topicDiscussionMapper;

    @Test
    public void testGetDiscussionsByTopicId() {
        Long topicId = 1L;
        List<TopicDiscussionDTO> discussions = topicDiscussionService.getDiscussionsByTopicId(topicId);
        assertNotNull(discussions);
        assertFalse(discussions.isEmpty());
        assertEquals("How to center text in HTML", discussions.get(0).getTitle());
    }

    @Test
    public void testGetDiscussionById() {
        Long discussionId = 1L;
        TopicDiscussionDTO discussion = topicDiscussionService.getDiscussionById(discussionId);
        assertNotNull(discussion);
        assertEquals("How to use div tags effectively", discussion.getTitle());
    }

    @Test
    public void testCreateDiscussion() {
        Long topicId = 1L;
        Long userId = 1L;
        String title = "New Discussion";
        String content = "Discussion content";
        TopicDiscussionDTO discussion = topicDiscussionService.createDiscussion(topicId, userId, title, content);
        assertNotNull(discussion);
        assertEquals(title, discussion.getTitle());
    }

    @Test
    public void testUpdateDiscussion() {
        Long discussionId = 1L;
        String updatedTitle = "Updated Title";
        String updatedContent = "Updated content";
        TopicDiscussionDTO updatedDiscussion = topicDiscussionService.updateDiscussion(discussionId, updatedTitle, updatedContent);
        assertNotNull(updatedDiscussion);
        assertEquals(updatedTitle, updatedDiscussion.getTitle());
    }

    @Test
    public void testDeleteDiscussion() {
        Long discussionId = 1L;
        topicDiscussionService.deleteDiscussion(discussionId);
        Exception exception = assertThrows(EntityNotFoundException.class, () -> topicDiscussionService.getDiscussionById(discussionId));
        assertTrue(exception.getMessage().contains("Discussion not found"));
    }

    @Test
    public void testGetDiscussionsByCourseId() {
        Long courseId = 1L;
        List<TopicDiscussionDTO> discussions = topicDiscussionService.getDiscussionsByCourseId(courseId);
        assertNotNull(discussions);
        assertFalse(discussions.isEmpty());
    }

    @Test
    public void testGetCourseId() {
        Long discussionId = 1L;
        Long courseId = topicDiscussionService.getCourseId(discussionId);
        assertNotNull(courseId);
        assertEquals(1L, courseId);
    }
} 