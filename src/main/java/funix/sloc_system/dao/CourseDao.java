package funix.sloc_system.dao;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CourseDao extends JpaRepository<Course, Long> {
    List<Course> findByCategoryId(Long categoryId);

    @Query("SELECT c FROM Course c JOIN c.instructor i WHERE i = :instructor AND c.status = :status")
    List<Course> findAllByInstructorAndStatus(@Param("instructor") User instructor, @Param("status") CourseStatus status);

    List<Course> findByInstructor(User instructor);

    List<Course> findByStatusIn(List<CourseStatus> statuses);

    List<Course> findByStatus(CourseStatus status);

    // get list of courses by status
    List<Course> findByStatusOrderByIdDesc(CourseStatus status);

    // get list of courses by title
    List<Course> findByTitleContainingIgnoreCase(String title);
}
