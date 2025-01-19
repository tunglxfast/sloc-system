package funix.sloc_system.service;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.UserRepository;
import funix.sloc_system.util.AppUtil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppUtil appUtil;

    private final long INSTRUCTOR_ID = 3L;
    private final long COURSE_ID = 1L;

    @Test
    public void testDeleteCourse() throws Exception {
        Long courseId = COURSE_ID; 
        Long instructorId = INSTRUCTOR_ID; 
        String result = courseService.deleteCourse(courseId, instructorId);
        assertEquals("Delete course request is sent, please wait for approval.", result);
    }

    @Test
    public void testDeleteDraftCourse() throws Exception {
        Course existCourse = courseRepository.findById(COURSE_ID).orElse(null);
        Course course = new Course();
        course.setTitle(existCourse.getTitle());
        course.setDescription(existCourse.getDescription());
        course.setCategory(existCourse.getCategory());
        course.setThumbnailUrl(existCourse.getThumbnailUrl());
        course.setCreatedBy(existCourse.getCreatedBy());
        course.setInstructor(existCourse.getInstructor());
        course.setStartDate(existCourse.getStartDate());
        course.setEndDate(existCourse.getEndDate());
        courseRepository.save(course);
        String result = courseService.deleteCourse(course.getId(), course.getInstructor().getId());
        assertEquals("Course deleted successfully.", result);
    }

    @Test
    public void testDeleteCourseRequest() throws Exception {
        Long courseId = COURSE_ID; 
        Long instructorId = INSTRUCTOR_ID; 
        String result = courseService.deleteCourse(courseId, instructorId);
        assertEquals("Delete course request is sent, please wait for approval.", result);
    }

    @Test
    public void testUpdateCourseStatus() {
        Long courseId = COURSE_ID; 
        courseService.updateCourseStatus(courseId, ApprovalStatus.APPROVED, ContentStatus.PUBLISHED);
        Course course = courseRepository.findById(courseId).orElse(null);
        assertNotNull(course);
        assertEquals(ApprovalStatus.APPROVED, course.getApprovalStatus());
        assertEquals(ContentStatus.PUBLISHED, course.getContentStatus());
    }

    @Test
    public void testUpdateCourseInstructor() {
        Long courseId = COURSE_ID; 
        Long instructorId = INSTRUCTOR_ID; 
        courseService.updateCourseInstructor(courseId, instructorId);
        Course course = courseRepository.findById(courseId).orElse(null);
        assertNotNull(course);
        User instructor = userRepository.findById(instructorId).orElse(null);
        assertEquals(instructor, course.getInstructor());
    }

    @Test
    public void testGetTopThreeCourses() {
        List<CourseDTO> result = courseService.getTopThreeCourses();
        assertEquals(3, result.size());
    }
} 