package funix.sloc_system.repository;

import funix.sloc_system.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByChapterId(Long chapterId);
}
