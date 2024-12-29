package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.ContentChangeTemporary;
import funix.sloc_system.entity.Course;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.mapper.ChapterMapper;
import funix.sloc_system.repository.ContentChangeRepository;
import funix.sloc_system.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ModeratorService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ContentChangeRepository contentChangeRepository;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChapterMapper chapterMapper;

    public List<CourseDTO> getPendingReviewCourses() {
        List<CourseDTO> pendingCourses = new ArrayList<>();
        
        // Get courses that are READY_TO_REVIEW (new courses)
        List<Course> newCourses = courseRepository.findByContentStatus(ContentStatus.READY_TO_REVIEW);
        for (Course course : newCourses) {
            pendingCourses.add(courseMapper.toDTO(course));
        }

        // Get courses that are PUBLISHED_EDITING (existing courses with updates)
        List<Course> editingCourses = courseRepository.findByContentStatus(ContentStatus.PUBLISHED_EDITING);
        for (Course course : editingCourses) {
            Optional<ContentChangeTemporary> contentChange = contentChangeRepository
                .findByEntityTypeAndEntityId(EntityType.COURSE, course.getId());
            
            if (contentChange.isPresent()) {
                try {
                    // Convert JSON content to CourseDTO
                    CourseDTO updatedCourse = objectMapper.readValue(
                        contentChange.get().getChanges(), 
                        CourseDTO.class
                    );
                    pendingCourses.add(updatedCourse);
                } catch (Exception e) {
                    // Log error and skip this course
                    e.printStackTrace();
                }
            }
        }

        return pendingCourses;
    }

    public CourseDTO getCourseForReview(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.getContentStatus() == ContentStatus.PUBLISHED_EDITING) {
            // Get updated content from temporary table
            Optional<ContentChangeTemporary> contentChange = contentChangeRepository
                .findByEntityTypeAndEntityId(EntityType.COURSE, courseId);
            
            if (contentChange.isPresent()) {
                try {
                    return objectMapper.readValue(
                        contentChange.get().getChanges(), 
                        CourseDTO.class
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Error reading course changes", e);
                }
            }
        }

        // For READY_TO_REVIEW status or if no changes found
        return courseMapper.toDTO(course);
    }

    @Transactional
    public void approveCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.getContentStatus() == ContentStatus.PUBLISHED_EDITING) {
            // Apply changes from temporary table
            Optional<ContentChangeTemporary> contentChange = contentChangeRepository
                .findByEntityTypeAndEntityId(EntityType.COURSE, courseId);
            
            if (contentChange.isPresent()) {
                try {
                    CourseDTO updatedCourse = objectMapper.readValue(
                        contentChange.get().getChanges(), 
                        CourseDTO.class
                    );
                    course.updateWithOtherCourse(courseMapper.toEntity(updatedCourse, course.getInstructor()));
                    course.getChapters().clear();
                    course.getChapters().addAll(chapterMapper.toEntity(updatedCourse.getChapters(), course));
                    contentChangeRepository.delete(contentChange.get());
                } catch (Exception e) {
                    throw new RuntimeException("Error applying course changes", e);
                }
            }
        }

        course.setContentStatus(ContentStatus.PUBLISHED);
        course.setApprovalStatus(ApprovalStatus.APPROVED);
        course.setChaptersContentStatus(ContentStatus.PUBLISHED);
        course.setRejectReason(null);
        courseRepository.save(course);

        // Send notification email
        // TODO: Implement email service
        // emailService.sendApprovalEmail(course.getInstructor(), course);
    }

    @Transactional
    public void rejectCourse(Long courseId, String rejectionReason) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.getContentStatus() == ContentStatus.READY_TO_REVIEW) {
            course.setContentStatus(ContentStatus.DRAFT);
        } else if (course.getContentStatus() == ContentStatus.PUBLISHED_EDITING) {
            course.setContentStatus(ContentStatus.PUBLISHED);
        }

        course.setApprovalStatus(ApprovalStatus.REJECTED);
        course.setRejectReason(rejectionReason);
        courseRepository.save(course);

        // Send notification email
        // TODO: Implement email service
        // emailService.sendRejectionEmail(course.getInstructor(), course, rejectionReason);
    }

    public ChapterDTO findChapterBySequence(CourseDTO courseDTO, int chapterNumber) {
        for (ChapterDTO chapter : courseDTO.getChapters()) {
            if (chapter.getSequence() == chapterNumber) {
                return chapter;
            }
        }
        throw new RuntimeException("Chapter not found");
    }

    public TopicDTO findTopicBySequence(ChapterDTO chapter, int topicNumber) {
        for (TopicDTO topic : chapter.getTopics()) {
            if (topic.getSequence() == topicNumber) {
                return topic;
            }
        }
        throw new RuntimeException("Topic not found");
    }
} 