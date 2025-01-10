package funix.sloc_system.repository;

import funix.sloc_system.entity.Category;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;


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

    // get list of courses by category
    List<Course> findByCategory(Category category);

    // get list of courses by title containing specific text and matching category
    List<Course> findByTitleContainingIgnoreCaseAndCategory(String title, Category category);

    Set<Course> findByEndDateBeforeAndContentStatusNot(LocalDate localDate, ContentStatus contentStatus);

    Page<Course> findByContentStatusIn(List<ContentStatus> contentStatuses, Pageable pageable);

    Page<Course> findByCategoryAndContentStatusIn(
        Category category, List<ContentStatus> contentStatuses, Pageable pageable);

    Page<Course> findByTitleContainingIgnoreCaseAndContentStatusIn(
            String title, List<ContentStatus> contentStatuses, Pageable pageable);

    Page<Course> findByTitleContainingIgnoreCaseAndCategoryAndContentStatusIn(
        String title, Category category, List<ContentStatus> contentStatuses, Pageable pageable);

    List<Course> findByApprovalStatus(ApprovalStatus approvalStatus);

}
