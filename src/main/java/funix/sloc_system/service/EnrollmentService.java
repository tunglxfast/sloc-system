package funix.sloc_system.service;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Enrollment;
import funix.sloc_system.entity.User;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.EnrollmentRepository;
import funix.sloc_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EnrollmentService {
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseMapper courseMapper;

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
        if (user == null) {
            return new HashSet<>();
        }
        return enrollmentRepository.findByUser(user);
    }

    public boolean checkEnrollment(User user, Course course) {
        return enrollmentRepository.existsByUserAndCourse(user, course);
    }

    public Set<Enrollment> getEnrollmentsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return new HashSet<>();
        }
        return enrollmentRepository.findByCourse(course);
    }

    /**
     * Find all courses that user enrolled and return as courseDTO set.
     * @param userId
     * @return
     */
    @Transactional
    public List<CourseDTO> getUserEnrollCourses(Long userId) {
        Set<Enrollment> enrollments = getEnrollmentsByUserId(userId);
        Set<CourseDTO> courseDTOSet = new HashSet<>();
        CourseDTO courseDTO; 
        for (Enrollment enrollment : enrollments) {
            courseDTO = courseMapper.toDTO(enrollment.getCourse());
            if (courseDTO != null) {
                courseDTOSet.add(courseDTO);
            }
        }
        return courseDTOSet.stream().sorted(Comparator.comparing(CourseDTO::getId)).toList();
    }

    @Transactional
    public List<Long> getEnrolledCourseIds(Long userId) {
        Set<Enrollment> enrollments = getEnrollmentsByUserId(userId);
        List<Long> courseIds = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            courseIds.add(enrollment.getCourse().getId());
        }
        return courseIds;
    }
    

    public Page<Course> getCoursesByUserIdWithPagination(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);  // Set page size and number
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByUserId(userId, pageable);
        return enrollmentPage.map(Enrollment::getCourse);
    }
}
