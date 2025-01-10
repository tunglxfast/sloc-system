package funix.sloc_system.repository;

import funix.sloc_system.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTopicDiscussionId(Long topicDiscussionId);
    List<Comment> findByCreatedById(Long userId);
} 