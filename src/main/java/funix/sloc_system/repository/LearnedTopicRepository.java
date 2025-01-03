package funix.sloc_system.repository;

import funix.sloc_system.entity.LearnedTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearnedTopicRepository extends JpaRepository<LearnedTopic, Long> {

    boolean existsByUserIdAndTopicId(Long userId, Long topicId);
}
