package funix.sloc_system.dao;

import funix.sloc_system.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicDao extends JpaRepository<Topic, Long> {

    List<Topic> findByChapterId(Long chapterId);
}
