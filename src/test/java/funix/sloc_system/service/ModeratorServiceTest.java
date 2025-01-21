package funix.sloc_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.repository.ContentChangeRepository;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.util.AppUtil;
import funix.sloc_system.util.ReviewCourseHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class ModeratorServiceTest {

    @MockBean
    private EmailService mailSender;

    @Autowired
    private ModeratorService moderatorService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ContentChangeRepository contentChangeRepository;

    @Autowired
    private AppUtil appUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CourseMapper courseMapper;

    private final Long READY_TO_REVIEW_COURSE_ID = 1L;
    private final Long PUBLISHED_EDITING_COURSE_ID = 2L;
    private final Long ERROR_COURSE_ID = 3L;
    private final Long DELETE_COURSE_ID = 4L;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        // make course 1L pending READY_TO_REVIEW
        Course course = courseRepository.findById(READY_TO_REVIEW_COURSE_ID).orElse(null);
        course.setApprovalStatus(ApprovalStatus.PENDING);
        course.setContentStatus(ContentStatus.READY_TO_REVIEW);
        courseRepository.save(course);

        // make course 2L pending PUBLISHED_EDITING
        Course course_2 = courseRepository.findById(PUBLISHED_EDITING_COURSE_ID).orElse(null);
        course_2.setApprovalStatus(ApprovalStatus.PENDING);
        course_2.setContentStatus(ContentStatus.PUBLISHED_EDITING);
        courseRepository.save(course_2);

        CourseDTO courseDTO_2 = courseMapper.toDTO(course_2);
        String json_2 = objectMapper.writeValueAsString(courseDTO_2);
        appUtil.saveContentChange(json_2, course_2.getId(), course_2.getInstructor().getId(), ContentAction.UPDATE);

        // make course 3L pending PUBLISHED_EDITING but not save ContentChangeTemporary
        Course course_3 = courseRepository.findById(ERROR_COURSE_ID).orElse(null);
        course_3.setContentStatus(ContentStatus.PUBLISHED_EDITING);
        courseRepository.save(course_3);

        // make course 4L pending PUBLISHED_EDITING but action is DELETE
        Course course_4 = courseRepository.findById(DELETE_COURSE_ID).orElse(null);
        course_4.setApprovalStatus(ApprovalStatus.PENDING);
        course_4.setContentStatus(ContentStatus.PUBLISHED_EDITING);
        courseRepository.save(course_4);

        CourseDTO courseDTO_4 = courseMapper.toDTO(course_4);
        String json_4 = objectMapper.writeValueAsString(courseDTO_4);
        appUtil.saveContentChange(json_4, course_4.getId(), course_4.getInstructor().getId(), ContentAction.DELETE);
    }

    @Test
    public void testGetPendingReviewCourses() {
        List<ReviewCourseHolder> reviewCourses = moderatorService.getPendingReviewCourses();
        assertNotNull(reviewCourses);
        assertFalse(reviewCourses.isEmpty());
    }

    @Test
    public void testGetCourseForReview_READY_TO_REVIEW_Course() {
        Course course = courseRepository.findById(READY_TO_REVIEW_COURSE_ID).orElse(null);
        assertNotNull(course);
        ReviewCourseHolder reviewCourseHolder = moderatorService.getCourseForReview(course.getId());
        assertNotNull(reviewCourseHolder);
        assertEquals(ContentAction.CREATE.name(), reviewCourseHolder.getAction());
        assertEquals(course.getId(), reviewCourseHolder.getCourse().getId());
    }

    @Test
    public void testGetCourseForReview_PUBLISHED_EDITING_Course() {
        Course course = courseRepository.findById(PUBLISHED_EDITING_COURSE_ID).orElse(null);
        assertNotNull(course);
        ReviewCourseHolder reviewCourseHolder = moderatorService.getCourseForReview(course.getId());
        assertNotNull(reviewCourseHolder);
        assertEquals(ContentAction.UPDATE.name(), reviewCourseHolder.getAction());
        assertEquals(course.getId(), reviewCourseHolder.getCourse().getId());
    }

    @Test
    public void testGetCourseForReview_Error_ReviewCourseHolderNull() {
        Course course = courseRepository.findById(ERROR_COURSE_ID).orElse(null);
        assertNotNull(course);
        ReviewCourseHolder reviewCourseHolder = moderatorService.getCourseForReview(course.getId());
        assertNull(reviewCourseHolder);
    }

    @Test
    public void testGetCourseForReview_DeleteCourse() {
        Course course = courseRepository.findById(DELETE_COURSE_ID).orElse(null);
        assertNotNull(course);
        ReviewCourseHolder reviewCourseHolder = moderatorService.getCourseForReview(course.getId());
        assertNotNull(reviewCourseHolder);
        assertEquals(ContentAction.DELETE.name(), reviewCourseHolder.getAction());
        assertEquals(course.getId(), reviewCourseHolder.getCourse().getId());
    }

    @Test
    public void testApproveCourse_REVIEW_COURSE() {
        Course course = courseRepository.findById(READY_TO_REVIEW_COURSE_ID).orElse(null);
        assertNotNull(course);

        assertDoesNotThrow(() -> moderatorService.approveCourse(course.getId()));
        Course approvedCourse = courseRepository.findById(course.getId()).orElse(null);
        assertNotNull(approvedCourse);
        assertEquals(ApprovalStatus.APPROVED, approvedCourse.getApprovalStatus());
    }

    @Test
    public void testApproveCourse_PUBLISHED_EDITING() {
        Course course = courseRepository.findById(PUBLISHED_EDITING_COURSE_ID).orElse(null);
        assertNotNull(course);

        assertDoesNotThrow(() -> moderatorService.approveCourse(course.getId()));
        Course approvedCourse = courseRepository.findById(course.getId()).orElse(null);
        assertNotNull(approvedCourse);
        assertEquals(ApprovalStatus.APPROVED, approvedCourse.getApprovalStatus());
    }

    @Test
    public void testApproveCourse_Error_CourseNotPENDING() {
        Course course = courseRepository.findById(ERROR_COURSE_ID).orElse(null);
        assertNotNull(course);
        Exception exception = assertThrows(RuntimeException.class, () -> moderatorService.approveCourse(course.getId()));
        assertTrue(exception.getMessage().contains("Course is not requested for review"));
    }

    @Test
    public void testRejectCourse_REVIEW_COURSE() {
        Course course = courseRepository.findById(READY_TO_REVIEW_COURSE_ID).orElse(null);

        assertDoesNotThrow(() -> moderatorService.rejectCourse(course.getId(), "Not suitable"));
        Course rejectedCourse = courseRepository.findById(course.getId()).orElse(null);
        assertEquals(ApprovalStatus.REJECTED, rejectedCourse.getApprovalStatus());
        assertEquals("Not suitable", rejectedCourse.getRejectReason());
    }

    @Test
    public void testRejectCourse_PUBLISHED_EDITING() {
        Course course = courseRepository.findById(PUBLISHED_EDITING_COURSE_ID).orElse(null);

        assertDoesNotThrow(() -> moderatorService.rejectCourse(course.getId(), "Not suitable"));
        Course rejectedCourse = courseRepository.findById(course.getId()).orElse(null);
        assertEquals(ApprovalStatus.REJECTED, rejectedCourse.getApprovalStatus());
        assertEquals("Not suitable", rejectedCourse.getRejectReason());
    }

    @Test
    public void testRejectCourse_Error_CourseNotPENDING() {
        Course course = courseRepository.findById(ERROR_COURSE_ID).orElse(null);
        assertNotNull(course);
        Exception exception = assertThrows(RuntimeException.class,
                () -> moderatorService.rejectCourse(course.getId(), "Not suitable"));
        assertTrue(exception.getMessage().contains("Course is not requested for review"));
    }

    @Test
    public void testFindChapterBySequence() throws Exception {
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(READY_TO_REVIEW_COURSE_ID);
        ChapterDTO foundChapter = moderatorService.findChapterBySequence(courseDTO, 1);
        assertNotNull(foundChapter);
        assertEquals(1, foundChapter.getSequence());
    }

    @Test
    public void testFindChapterBySequence_ChapterNotExist_Exception() throws Exception {
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(READY_TO_REVIEW_COURSE_ID);
        Exception exception = assertThrows(RuntimeException.class,
                () -> moderatorService.findChapterBySequence(courseDTO, 999));
        assertTrue(exception.getMessage().contains("Chapter not found"));
    }

    @Test
    public void testFindTopicBySequence() throws Exception {
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(READY_TO_REVIEW_COURSE_ID);
        ChapterDTO chapterDTO = courseDTO.getChapters().get(0);
        TopicDTO foundTopic = moderatorService.findTopicBySequence(chapterDTO, 1);
        assertNotNull(foundTopic);
        assertEquals(1, foundTopic.getSequence());
    }

    @Test
    public void testFindTopicBySequence_TopicNotExist_Exception() throws Exception {
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(READY_TO_REVIEW_COURSE_ID);
        ChapterDTO chapterDTO = courseDTO.getChapters().get(0);
        Exception exception = assertThrows(RuntimeException.class,
                () -> moderatorService.findTopicBySequence(chapterDTO, 999));
        assertTrue(exception.getMessage().contains("Topic not found"));
    }
} 