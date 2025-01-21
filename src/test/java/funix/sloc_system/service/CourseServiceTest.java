package funix.sloc_system.service;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.UserRepository;
import funix.sloc_system.util.AppUtil;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    private CourseMapper courseMapper;

    @Autowired
    private AppUtil appUtil;

    private final long INSTRUCTOR_ID = 3L;
    private final long COURSE_ID = 1L;
    private final long NULL_ID = 999L;
    private final String WEB_DEVELOPMENT = "Web Development";
    private final String WEB_DEVELOPMENT_TITLE = "Introduction to Web Development";
    private final String NOT_EXIST = "not exist";
    private MockMultipartFile thumbnailFile;

    @BeforeEach
    public void setUp() {
        // prepare thumbnail
        thumbnailFile = new MockMultipartFile("thumbnailFile", "thumbnail.png", "image/png", "thumbnailFile".getBytes()) {
            @Override
            public void transferTo(File dest){
                // do nothing
            }
        };
    }

    @Test
    public void testGetAllCourses() {
        List<Course> courseList = courseService.getAllCourses();
        assertNotNull(courseList);
        assertTrue(courseList.size() == 5);
    }

    @Test
    public void testGetAllCoursesDTO() {
        List<CourseDTO> courseList = courseService.getAllCoursesDTO();
        assertNotNull(courseList);
        assertTrue(courseList.size() == 5);
    }

    @Test
    public void testArchiveExpiredCourses() {
        courseService.archiveExpiredCourses();
        List<Course> courseList = courseService.getAllCourses();
        boolean haveArchived = false;
        for (Course course : courseList) {
            if (course.getContentStatus().equals(ContentStatus.ARCHIVED)) {
                haveArchived = true;
                break;
            }
        }
        assertTrue(haveArchived);
    }

    @Test
    public void testFindByCategoryId() {
        List<Course> courseList = courseService.findByCategoryId(COURSE_ID);
        assertNotNull(courseList);
        assertTrue(courseList.size() == 1);
    }

    @Test
    public void testSaveCourse() {
        Course existCourse = courseService.getAllCourses().get(0);
        Course newCourse = new Course();
        newCourse.setTitle("New Course");
        newCourse.setCategory(existCourse.getCategory());
        newCourse.setInstructor(existCourse.getInstructor());
        newCourse.setCreatedBy(existCourse.getCreatedBy());
        Course course = courseService.save(newCourse);
        assertNotNull(course);
        assertEquals("New Course", course.getTitle());
        assertNotNull(course.getId());
    }

    @Test
    public void testGetInstructorRejectedCourses(){
        User instructor = userRepository.findById(INSTRUCTOR_ID).orElse(null);
        List<Course> courses = courseService.getInstructorRejectedCourses(instructor);
        assertNotNull(courses);
        assertTrue(courses.size() == 0);
    }

    @Test
    public void testGetAvailableCourses() {
        List<Course> courses = courseService.getAvailableCourses();
        assertNotNull(courses);
        assertTrue(courses.size() == 4);
    }

    @Test
    public void testFindCoursesByTitle() {
        List<Course> courses = courseService.findCoursesByTitle(WEB_DEVELOPMENT_TITLE);
        assertNotNull(courses);
        assertTrue(courses.size() == 1);
    }

    @Test
    public void testFindCoursesByCategory() {
        List<Course> courses = courseService.findCoursesByCategory(WEB_DEVELOPMENT);
        assertNotNull(courses);
        assertTrue(courses.size() == 1);
    }

    @Test
    public void testFindCoursesByCategory_WrongCategory() {
        List<Course> courses = courseService.findCoursesByCategory(NOT_EXIST);
        assertNotNull(courses);
        assertTrue(courses.size() == 0);
    }

    @Test
    public void testFindCoursesByTitleAndCategory() {
        List<Course> courses = courseService.findCoursesByTitleAndCategory(WEB_DEVELOPMENT_TITLE, WEB_DEVELOPMENT);
        assertNotNull(courses);
        assertTrue(courses.size() == 1);
    }

    @Test
    public void testFindCoursesByTitleAndCategory_WrongCategory() {
        List<Course> courses = courseService.findCoursesByTitleAndCategory(WEB_DEVELOPMENT_TITLE, NOT_EXIST);
        assertNotNull(courses);
        assertTrue(courses.size() == 0);
    }

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
    public void testUpdateCourseStatus_CourseNull() {
        Long courseId = NULL_ID;
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> courseService.updateCourseStatus(courseId, ApprovalStatus.APPROVED, ContentStatus.PUBLISHED));
        assertTrue(exception.getMessage().contains("Course not found"));
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

    public void testUpdateCourseInstructor_InstrutorNull() {
        Long courseId = COURSE_ID;
        Long instructorId = null;
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> courseService.updateCourseInstructor(courseId, instructorId));

        assertTrue(exception.getMessage().contains("Instructor not found"));
    }

    public void testUpdateCourseInstructor_CourseNull() {
        Long courseId = null;
        Long instructorId = INSTRUCTOR_ID;
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> courseService.updateCourseInstructor(courseId, instructorId));

        assertTrue(exception.getMessage().contains("Course not found"));
    }

    @Test
    public void testGetTopThreeCourses() {
        List<CourseDTO> result = courseService.getTopThreeCourses();
        assertEquals(3, result.size());
    }

    @Test
    public void testSubmitForReview_CourseNotExist() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> courseService.submitForReview(NULL_ID));
        assertTrue(exception.getMessage().contains("Course not found!"));
    }

    @Test
    public void testSubmitForReview_ChapterEmpty() {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        course.setChapters(new ArrayList<>());
        courseRepository.save(course);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> courseService.submitForReview(COURSE_ID));
        assertTrue(exception.getMessage().contains("Course must have at least one chapter before submit for review."));
    }

    @Test
    public void testSubmitForReview_DraftCourse() {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        course.setContentStatus(ContentStatus.DRAFT);
        courseRepository.save(course);
        courseService.submitForReview(COURSE_ID);

        Course courseAfter = courseRepository.findById(COURSE_ID).orElse(null);
        assertNotNull(courseAfter);
        assertEquals(ContentStatus.READY_TO_REVIEW, courseAfter.getContentStatus());
    }

    @Test
    public void testSubmitForReview_PublishCourse() {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        course.setContentStatus(ContentStatus.PUBLISHED);
        courseRepository.save(course);
        courseService.submitForReview(COURSE_ID);

        Course courseAfter = courseRepository.findById(COURSE_ID).orElse(null);
        assertNotNull(courseAfter);
        assertEquals(ContentStatus.PUBLISHED_EDITING, courseAfter.getContentStatus());
    }

    @Test
    public void testFindByInstructor() {
        User instructor = userRepository.findById(INSTRUCTOR_ID).orElse(null);
        List<Course> courses = courseService.findByInstructor(instructor);
        assertNotNull(courses);
        assertTrue(courses.size() == 5);
    }

    @Test
    public void testCreateNewCourse() throws Exception {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        CourseDTO exitedCourseDTO = courseMapper.toDTO(course);

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setTitle("New Course");
        courseDTO.setCategory(exitedCourseDTO.getCategory());
        courseDTO.setInstructor(exitedCourseDTO.getInstructor());
        courseDTO.setCreatedBy(exitedCourseDTO.getCreatedBy());
        courseDTO.setStartDate(exitedCourseDTO.getStartDate());
        courseDTO.setEndDate(exitedCourseDTO.getEndDate());

        CourseDTO returnCourseDTO = courseService.createNewCourse(courseDTO, INSTRUCTOR_ID, thumbnailFile, course.getCategory().getId());

        assertNotNull(returnCourseDTO);
        assertNotNull(returnCourseDTO.getId());

        assertEquals("New Course", returnCourseDTO.getTitle());
        assertNotEquals(returnCourseDTO.getId(), exitedCourseDTO.getId());
    }

    @Test
    public void testUpdateCourse() throws Exception {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setTitle("New Course");

        courseService.updateCourse(COURSE_ID, courseDTO, INSTRUCTOR_ID, thumbnailFile, course.getCategory().getId());

        CourseDTO afterCourse = appUtil.getEditingCourseDTO(COURSE_ID);

        assertNotNull(afterCourse);
        assertEquals("New Course", afterCourse.getTitle());
        assertEquals(afterCourse.getId(), afterCourse.getId());
    }

    @Test
    public void testUpdateCourse_CourseDraft() throws Exception {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        course.setContentStatus(ContentStatus.DRAFT);
        courseRepository.save(course);

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setTitle("New Course");

        courseService.updateCourse(COURSE_ID, courseDTO, INSTRUCTOR_ID, thumbnailFile, course.getCategory().getId());

        Course afterCourse = courseRepository.findById(COURSE_ID).orElse(null);

        assertNotNull(afterCourse);
        assertEquals("New Course", afterCourse.getTitle());
        assertEquals(afterCourse.getId(), afterCourse.getId());
    }

    @Test
    public void testUpdateCourse_InstructorNull() throws Exception {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setTitle("New Course");

        Exception exception = assertThrows(RuntimeException.class,
                () -> courseService.updateCourse(COURSE_ID, courseDTO, NULL_ID, thumbnailFile, course.getCategory().getId()));

        assertTrue(exception.getMessage().contains("Missing user information, please login again."));
    }

    @Test
    public void testUpdateCourse_CategoryNull() throws Exception {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setTitle("New Course");

        Exception exception = assertThrows(RuntimeException.class,
                () -> courseService.updateCourse(COURSE_ID, courseDTO, INSTRUCTOR_ID, thumbnailFile, NULL_ID));

        assertTrue(exception.getMessage().contains("Category information error, please contact support center."));
    }

    @Test
    public void testCheckCourseExists() {
        assertTrue(courseService.checkCourseExists(COURSE_ID));
    }

    @Test
    public void testResetCourse_ThrowsException() {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        course.setApprovalStatus(ApprovalStatus.PENDING);
        courseRepository.save(course);
        assertThrows(IllegalStateException.class, () -> courseService.resetCourse(COURSE_ID, INSTRUCTOR_ID));
    }

    @Test
    public void testResetCourse() {
        Course course = courseRepository.findById(COURSE_ID).orElse(null);
        assertDoesNotThrow(() -> courseService.resetCourse(COURSE_ID, INSTRUCTOR_ID));
    }

    @Test
    public void testGetCoursesWithPagination() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService.getCoursesWithPagination(pageable);
        assertTrue(coursePage.getTotalPages() == 2);
        assertTrue(coursePage.getTotalElements() == 4);
    }

    @Test
    public void testSearchCoursesByCategoryWithPagination() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService.searchCoursesByCategoryWithPagination(WEB_DEVELOPMENT, pageable);
        assertEquals(1, coursePage.getTotalPages());
        assertEquals(1, coursePage.getTotalElements());
    }

    @Test
    public void testSearchCoursesByCategoryWithPagination_CategoryNotExist() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService.searchCoursesByCategoryWithPagination(NOT_EXIST, pageable);
        assertTrue(coursePage.isEmpty());
    }

    @Test
    public void testSearchCoursesByCategoryWithPagination_CategoryNull() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService.searchCoursesByCategoryWithPagination(null, pageable);
        assertTrue(coursePage.isEmpty());
    }

    @Test
    public void testSearchCoursesByTitleWithPagination() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService.searchCoursesByTitleWithPagination(WEB_DEVELOPMENT_TITLE, pageable);
        assertEquals(1, coursePage.getTotalPages());
        assertEquals(1, coursePage.getTotalElements());
    }

    @Test
    public void testSearchCoursesByTitleWithPagination_TitleNull() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService.searchCoursesByTitleWithPagination(null, pageable);
        assertTrue(coursePage.isEmpty());
    }

    @Test
    public void testSearchCoursesByTitleAndCategoryWithPagination() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService
                .searchCoursesByTitleAndCategoryWithPagination(WEB_DEVELOPMENT_TITLE, WEB_DEVELOPMENT, pageable);
        assertEquals(1, coursePage.getTotalPages());
        assertEquals(1, coursePage.getTotalElements());
    }

    @Test
    public void testSearchCoursesByTitleAndCategoryWithPagination_TitleNull() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService
                .searchCoursesByTitleAndCategoryWithPagination(null, WEB_DEVELOPMENT, pageable);
        assertTrue(coursePage.isEmpty());
    }

    @Test
    public void testSearchCoursesByTitleAndCategoryWithPagination_CategoryNotExist() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService
                .searchCoursesByTitleAndCategoryWithPagination(WEB_DEVELOPMENT_TITLE, NOT_EXIST, pageable);
        assertTrue(coursePage.isEmpty());
    }

    @Test
    public void testGetCoursesPagination() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService.getCoursesPagination(WEB_DEVELOPMENT_TITLE, WEB_DEVELOPMENT, pageable);
        assertEquals(1, coursePage.getTotalPages());
        assertEquals(1, coursePage.getTotalElements());
    }

    @Test
    public void testGetCoursesPagination_TitleAndCategoryNull() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService.getCoursesPagination(null, null, pageable);
        assertTrue(coursePage.getTotalPages() == 2);
        assertTrue(coursePage.getTotalElements() == 4);
    }

    @Test
    public void testGetCoursesPagination_TitleNull() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService.getCoursesPagination(null, WEB_DEVELOPMENT, pageable);
        assertEquals(1, coursePage.getTotalPages());
        assertEquals(1, coursePage.getTotalElements());
    }
    @Test
    public void testGetCoursesPagination_CategoryNull() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Course> coursePage = courseService.getCoursesPagination(WEB_DEVELOPMENT_TITLE, null, pageable);
        assertEquals(1, coursePage.getTotalPages());
        assertEquals(1, coursePage.getTotalElements());
    }

    @Test
    public void testUpdateCourse_EndDateEqualStartDate_ThrowException() throws Exception {
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(COURSE_ID);
        courseDTO.setEndDate(courseDTO.getStartDate());
        Exception exception = assertThrows(RuntimeException.class,
                () -> courseService.updateCourse(COURSE_ID, courseDTO, INSTRUCTOR_ID, null, courseDTO.getCategory().getId()));
        assertTrue(exception.getMessage().contains("Course end date must before course start date at least 7 day!"));
    }

} 