package funix.sloc_system.repository;

import funix.sloc_system.entity.Chapter;
import funix.sloc_system.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    Set<Chapter> findByCourseId(Long courseId);

    Optional<Chapter> findByCourseIdAndSequence(Long courseId, int sequence);

    List<Chapter> findByCourseIdOrderBySequence(Long courseId);

    List<Chapter> findByCourseIdAndContentStatus(Long courseId, ContentStatus status);

    Optional<Chapter> findByTopicsId(Long topicId);
}
