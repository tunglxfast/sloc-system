package funix.sloc_system.repository;

import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByChapterId(Long chapterId);

    Optional<Topic> findByChapterIdAndSequence(Long chapterId, int sequence);

    List<Topic> findByChapterIdAndStatus(Long chapterId, CourseStatus status);
}
