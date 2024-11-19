package funix.sloc_system.dao;

import funix.sloc_system.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterDAO extends JpaRepository<Chapter, Long> {

    List<Chapter> findByCourseId(Long courseId);
}
