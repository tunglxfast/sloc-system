package funix.sloc_system.dao;

import funix.sloc_system.entity.Chapter;
import funix.sloc_system.enums.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterDao extends JpaRepository<Chapter, Long> {

    List<Chapter> findByCourseId(Long courseId);

    Optional<Chapter> findByCourseIdAndSequence(Long courseId, int sequence);

    List<Chapter> findByCourseIdOrderBySequence(Long courseId);

    List<Chapter> findByCourseIdAndStatus(Long courseId, CourseStatus status);
}
