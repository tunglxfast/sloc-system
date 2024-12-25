package funix.sloc_system.repository;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCategoryId(Long categoryId);

    List<Course> findAllByInstructorAndApprovalStatus(User instructor, ApprovalStatus approvalStatus);

    List<Course> findByInstructor(User instructor);

    List<Course> findByContentStatusIn(List<ContentStatus> statuses);

    List<Course> findByContentStatus(ContentStatus status);

    // get list of courses by content status
    List<Course> findByContentStatusOrderByIdDesc(ContentStatus status);

    // get list of courses by title
    List<Course> findByTitleContainingIgnoreCase(String title);
}