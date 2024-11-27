package funix.sloc_system.dao;

import funix.sloc_system.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CourseDao extends JpaRepository<Course, Long> {
    List<Course> findByCategoryId(Long categoryId);
}
