package funix.sloc_system.dao;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Enrollment;
import funix.sloc_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;


@Repository
public interface EnrollmentDao extends JpaRepository<Enrollment, Long> {
    Set<Enrollment> findByUser(User user);
    Set<Enrollment> findByCourse(Course course);

    boolean existsByUserAndCourse(User user, Course course);
}
