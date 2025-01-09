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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TopicDiscussionRepository topicDiscussionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentMapper commentMapper;

    public List<CommentDTO> getCommentsByDiscussionId(Long discussionId) {
        List<Comment> comments = commentRepository.findByTopicDiscussionId(discussionId);
        return comments.stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CommentDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        return commentMapper.toDTO(comment);
    }

    @Transactional
    public CommentDTO createComment(Long discussionId, Long userId, String content) {
        TopicDiscussion discussion = topicDiscussionRepository.findById(discussionId)
                .orElseThrow(() -> new EntityNotFoundException("Discussion not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setTopicDiscussion(discussion);
        comment.setCreatedBy(user);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        discussion.addComment(comment);
        Comment savedComment = commentRepository.save(comment);
        discussion.setUpdatedAt(LocalDateTime.now());
        topicDiscussionRepository.save(discussion);
        return commentMapper.toDTO(savedComment);
    }

    @Transactional
    public CommentDTO updateComment(Long id, String content) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDTO(savedComment);
    }

    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        TopicDiscussion discussion = comment.getTopicDiscussion();
        discussion.removeComment(comment);
        topicDiscussionRepository.save(discussion);
        commentRepository.delete(comment);
    }
} 
