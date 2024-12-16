package funix.sloc_system.service;

import funix.sloc_system.dao.CourseDao;
import funix.sloc_system.dao.EnrollmentDao;
import funix.sloc_system.dao.UserDao;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Enrollment;
import funix.sloc_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Service
public class EnrollmentService {
    @Autowired
    private EnrollmentDao enrollmentDAO;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CourseDao courseDao;

    @Transactional
    public String enrollCourse(User user, Course course) {
        // Kiểm tra xem khóa học có còn trong thời gian đăng ký không
        if (course.getStartDate().isAfter(LocalDate.now()) || course.getEndDate().isBefore(LocalDate.now())) {
            return "Course is not available for registration";
        }

        // Kiểm tra xem người dùng đã đăng ký khóa học chưa
        boolean isEnrolled = user.getEnrollments().stream()
                .anyMatch(enroll -> enroll.getCourse().getId().equals(course.getId()));
        if (isEnrolled) {
            return "User already enrolled this course";
        }

        // Tạo đăng ký mới
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(java.time.LocalDate.now());

        enrollmentDAO.save(enrollment);

        // sau khi đăng ký khóa học, cần cập nhật lại thông tin
        user.getEnrollments().add(enrollment);
        course.getEnrollments().add(enrollment);
        userDao.save(user);
        courseDao.save(course);

        return "Register successfully";
    }

    public Set<Enrollment> getEnrollmentsByUserId(Long userId) {
        User user = userDao.findById(userId).orElse(null);
        return enrollmentDAO.findByUser(user);
    }

    public boolean isEnrolled(User user, Course course) {
        return enrollmentDAO.existsByUserAndCourse(user, course);
    }

    public Set<Enrollment> getEnrollmentsByCourseId(Long courseId) {
        Course course = courseDao.findById(courseId).orElse(null);
        return enrollmentDAO.findByCourse(course);
    }
}
