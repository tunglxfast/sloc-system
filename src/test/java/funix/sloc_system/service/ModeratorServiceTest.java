package funix.sloc_system.service;

import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
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

    @BeforeEach
    private void setUp() {
      // make course 1L pending
      Course course = courseRepository.findById(1L).orElse(null);
      course.setApprovalStatus(ApprovalStatus.PENDING);
      course.setContentStatus(ContentStatus.READY_TO_REVIEW);
      courseRepository.save(course);
    }

    @Test
    public void testGetPendingReviewCourses() {
        List<ReviewCourseHolder> reviewCourses = moderatorService.getPendingReviewCourses();
        assertNotNull(reviewCourses);
        assertFalse(reviewCourses.isEmpty());
    }

    @Test
    public void testGetCourseForReview() {
        Course course = courseRepository.findById(1L).orElse(null);
        assertNotNull(course);
        ReviewCourseHolder reviewCourseHolder = moderatorService.getCourseForReview(course.getId());
        assertNotNull(reviewCourseHolder);
    }

    @Test
    public void testApproveCourse() {
        Course course = courseRepository.findById(1L).orElse(null);
        assertNotNull(course);
        course.setApprovalStatus(ApprovalStatus.PENDING);
        course.setContentStatus(ContentStatus.READY_TO_REVIEW);
        courseRepository.save(course);

        assertDoesNotThrow(() -> moderatorService.approveCourse(course.getId()));
        Course approvedCourse = courseRepository.findById(course.getId()).orElse(null);
        assertEquals(ApprovalStatus.APPROVED, approvedCourse.getApprovalStatus());
    }

    @Test
    public void testRejectCourse() {
        Course course = courseRepository.findById(1L).orElse(null);
        assertNotNull(course);
        course.setApprovalStatus(ApprovalStatus.PENDING);
        course.setContentStatus(ContentStatus.READY_TO_REVIEW);
        courseRepository.save(course);

        assertDoesNotThrow(() -> moderatorService.rejectCourse(course.getId(), "Not suitable"));
        Course rejectedCourse = courseRepository.findById(course.getId()).orElse(null);
        assertEquals(ApprovalStatus.REJECTED, rejectedCourse.getApprovalStatus());
        assertEquals("Not suitable", rejectedCourse.getRejectReason());
    }

    @Test
    public void testFindChapterBySequence() throws Exception {
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(1L);
        ChapterDTO foundChapter = moderatorService.findChapterBySequence(courseDTO, 1);
        assertNotNull(foundChapter);
        assertEquals(1, foundChapter.getSequence());
    }

    @Test
    public void testFindTopicBySequence() throws Exception {
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(1L);
        ChapterDTO chapterDTO = courseDTO.getChapters().get(0);
        TopicDTO foundTopic = moderatorService.findTopicBySequence(chapterDTO, 1);
        assertNotNull(foundTopic);
        assertEquals(1, foundTopic.getSequence());
    }
} 