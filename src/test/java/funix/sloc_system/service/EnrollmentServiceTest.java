package funix.sloc_system.service;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Enrollment;
import funix.sloc_system.entity.User;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private final Long STUDENT_ID = 4L;
    private final Long COURSE_ID = 1L;
    private final Long WRONG_ID = 999L;

    @Test
    public void testEnrollCourse() {
        User user = userRepository.findById(9L).orElse(null);
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        String result = enrollmentService.enrollCourse(user, course);
        assertEquals("Register successfully", result);
    }

    @Test
    public void testEnrollCourse_CourseNotAvailable() {
        User user = userRepository.findById(STUDENT_ID).orElse(null);
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        course.setStartDate(LocalDate.now().plusDays(1));
        String result = enrollmentService.enrollCourse(user, course);
        assertEquals("Course is not available for registration", result);
    }

    @Test
    public void testEnrollCourse_UserAlreadyEnrolled() {
        User user = userRepository.findById(STUDENT_ID).orElse(null);
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        enrollmentService.enrollCourse(user, course);
        String result = enrollmentService.enrollCourse(user, course);
        assertEquals("User already enrolled this course", result);
    }

    @Test
    public void testGetEnrollmentsByUserId() {
        Set<Enrollment> enrollments = enrollmentService.getEnrollmentsByUserId(STUDENT_ID);
        assertNotNull(enrollments);
        assertFalse(enrollments.isEmpty());
    }

    @Test
    public void testGetEnrollmentsByUserId_WrongId() {
        Set<Enrollment> enrollments = enrollmentService.getEnrollmentsByUserId(WRONG_ID);
        assertNotNull(enrollments);
        assertTrue(enrollments.isEmpty());
    }

    @Test
    public void testCheckEnrollment() {
        User user = userRepository.findById(STUDENT_ID).orElse(null);
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        assertTrue(enrollmentService.checkEnrollment(user, course));
    }

    @Test
    public void testGetEnrollmentsByCourseId() {
        Set<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourseId(COURSE_ID);
        assertNotNull(enrollments);
        assertFalse(enrollments.isEmpty());
    }

    @Test
    public void testGetEnrollmentsByCourseId_WrongId() {
        Set<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourseId(WRONG_ID);
        assertNotNull(enrollments);
        assertTrue(enrollments.isEmpty());
    }

    @Test
    public void testGetUserEnrollCourses() {
        List<CourseDTO> courses = enrollmentService.getUserEnrollCourses(STUDENT_ID);
        assertNotNull(courses);
        assertFalse(courses.isEmpty());
    }

    @Test
    public void testGetEnrolledCourseIds() {
        List<Long> courseIds = enrollmentService.getEnrolledCourseIds(STUDENT_ID);
        assertNotNull(courseIds);
        assertFalse(courseIds.isEmpty());
    }

    @Test
    public void testGetCoursesByUserIdWithPagination() {
        Page<Course> courses = enrollmentService.getCoursesByUserIdWithPagination(STUDENT_ID, 0, 10);
        assertNotNull(courses);
        assertFalse(courses.isEmpty());
    }
} 