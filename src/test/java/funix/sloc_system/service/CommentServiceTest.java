package funix.sloc_system.service;

import funix.sloc_system.dto.CommentDTO;
import funix.sloc_system.entity.Comment;
import funix.sloc_system.entity.TopicDiscussion;
import funix.sloc_system.entity.User;
import funix.sloc_system.mapper.CommentMapper;
import funix.sloc_system.repository.CommentRepository;
import funix.sloc_system.repository.TopicDiscussionRepository;
import funix.sloc_system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    private final long DISCUSSION_ID = 1L;
    private final long COMMENT_ID = 1L;
    private final long STUDENT_ID = 4L;

    @Test
    public void testGetCommentsByDiscussionId() {
        Long discussionId = DISCUSSION_ID;
        List<CommentDTO> comments = commentService.getCommentsByDiscussionId(discussionId);
        assertNotNull(comments);
    }

    @Test
    public void testGetCommentById() {
        Long commentId = COMMENT_ID;
        CommentDTO comment = commentService.getCommentById(commentId);
        assertNotNull(comment);
    }

    @Test
    public void testCreateComment() {
        Long discussionId = DISCUSSION_ID;
        Long userId = STUDENT_ID;
        String content = "Test comment";
        CommentDTO comment = commentService.createComment(discussionId, userId, content);
        assertNotNull(comment);
    }

    @Test
    public void testUpdateComment() {
        Long commentId = COMMENT_ID;
        String updatedContent = "Updated comment";
        CommentDTO updatedComment = commentService.updateComment(commentId, updatedContent);
        assertNotNull(updatedComment);
    }

    @Test
    public void testDeleteComment() {
        Long commentId = COMMENT_ID;
        commentService.deleteComment(commentId);
        assertThrows(EntityNotFoundException.class, () -> commentService.getCommentById(commentId));
    }
} 