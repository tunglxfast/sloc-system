package funix.sloc_system.repository;

import funix.sloc_system.entity.TopicDiscussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicDiscussionRepository extends JpaRepository<TopicDiscussion, Long> {
    List<TopicDiscussion> findByTopicId(Long topicId);
    List<TopicDiscussion> findByCreatedById(Long userId);
} 