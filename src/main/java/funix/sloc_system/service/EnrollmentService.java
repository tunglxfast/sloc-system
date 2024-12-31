package funix.sloc_system.service;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Enrollment;
import funix.sloc_system.entity.User;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.EnrollmentRepository;
import funix.sloc_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Service
public class EnrollmentService {
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public String enrollCourse(User user, Course course) {
        // Kiểm tra xem khóa học có còn trong thời gian đăng ký không
        if (course.getStartDate().isAfter(LocalDate.now()) || course.getEndDate().isBefore(LocalDate.now())) {
            return "Course is not available for registration";
        }

        // Kiểm tra xem người dùng đã đăng ký khóa học chưa
        boolean isEnrolled = checkEnrollment(user, course);
        if (isEnrolled) {
            return "User already enrolled this course";
        }

        // Tạo đăng ký mới
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(java.time.LocalDate.now());

        enrollmentRepository.save(enrollment);

        // sau khi đăng ký khóa học, cần cập nhật lại thông tin
        user.getEnrollments().add(enrollment);
        course.getEnrollments().add(enrollment);
        userRepository.save(user);
        courseRepository.save(course);

        return "Register successfully";
    }

    public Set<Enrollment> getEnrollmentsByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return enrollmentRepository.findByUser(user);
    }

    public boolean checkEnrollment(User user, Course course) {
        return enrollmentRepository.existsByUserAndCourse(user, course);
    }

    public Set<Enrollment> getEnrollmentsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        return enrollmentRepository.findByCourse(course);
    }
}
