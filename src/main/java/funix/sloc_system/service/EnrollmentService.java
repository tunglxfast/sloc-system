package funix.sloc_system.service;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Enrollment;
import funix.sloc_system.entity.User;
import funix.sloc_system.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public Enrollment enrollCourse(User user, Course course) {
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(java.time.LocalDate.now());

        // sau khi đăng ký khóa học, cần cập nhật lại
        user.getEnrollments().add(enrollment);
        course.getEnrollments().add(enrollment);

        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getEnrollmentsByUser(User user) {
        return enrollmentRepository.findByUser(user);
    }

    public boolean isEnrolled(User user, Course course) {
        return enrollmentRepository.existsByUserAndCourse(user, course);
    }

    public List<Enrollment> getEnrollmentsByCourse(Course course) {
        return enrollmentRepository.findByCourse(course);
    }
}
