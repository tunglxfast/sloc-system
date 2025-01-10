package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.ContentChangeTemporary;
import funix.sloc_system.entity.Course;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.mapper.ChapterMapper;
import funix.sloc_system.repository.ContentChangeRepository;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.util.ReviewCourseHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<ReviewCourseHolder> getPendingReviewCourses() {
        List<ReviewCourseHolder> reviewCourseHolders = new ArrayList<>();
        
        // Get courses that are READY_TO_REVIEW (new courses)
        List<Course> newCourses = courseRepository.findByContentStatus(ContentStatus.READY_TO_REVIEW);
        for (Course course : newCourses) {
            reviewCourseHolders.add(new ReviewCourseHolder(courseMapper.toDTO(course), ContentAction.CREATE));  
        }

        // Get courses that are PUBLISHED_EDITING (existing courses with updates)
        List<Course> editingCourses = courseRepository.findByContentStatus(ContentStatus.PUBLISHED_EDITING);
        for (Course course : editingCourses) {
            Optional<ContentChangeTemporary> contentChange = contentChangeRepository
                .findByEntityTypeAndEntityId(EntityType.COURSE, course.getId());

            if (contentChange.isPresent()) {
                ContentChangeTemporary contentChangeEntity = contentChange.get();
                ContentAction action = contentChangeEntity.getAction();
                if (ContentAction.UPDATE.equals(action)) {
                    try {
                        CourseDTO updatedCourse = objectMapper.readValue(
                            contentChangeEntity.getChanges(), 
                            CourseDTO.class
                        );
                        reviewCourseHolders.add(new ReviewCourseHolder(updatedCourse, contentChangeEntity.getAction()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (ContentAction.DELETE.equals(action)) {
                    reviewCourseHolders.add(new ReviewCourseHolder(courseMapper.toDTO(course)   , contentChangeEntity.getAction()));
                }
            }
        }

        return reviewCourseHolders;
    }

    public ReviewCourseHolder getCourseForReview(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        ContentStatus contentStatus = course.getContentStatus();
        if (ContentStatus.READY_TO_REVIEW.equals(contentStatus)) {
            return new ReviewCourseHolder(courseMapper.toDTO(course), ContentAction.CREATE);
        } 
        else if (ContentStatus.PUBLISHED_EDITING.equals(contentStatus)) {
            Optional<ContentChangeTemporary> contentChange = contentChangeRepository
                .findByEntityTypeAndEntityId(EntityType.COURSE, courseId);
            if (contentChange.isPresent()) {
                ContentChangeTemporary contentChangeEntity = contentChange.get();
                ContentAction action = contentChangeEntity.getAction();
                if (ContentAction.UPDATE.equals(action)) {
                    try {
                        CourseDTO updatedCourse = objectMapper.readValue(
                            contentChangeEntity.getChanges(), 
                            CourseDTO.class
                        );
                        return new ReviewCourseHolder(
                            updatedCourse, 
                            contentChangeEntity.getAction()
                        );
                    } catch (Exception e) {
                        throw new RuntimeException("Error reading course changes", e);
                    }
                } else if (ContentAction.DELETE.equals(action)) {
                    return new ReviewCourseHolder(courseMapper.toDTO(course), contentChangeEntity.getAction());
                }
            }
        }
        return null;
    }

    @Transactional
    public void approveCourse(Long courseId) throws RuntimeException {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        ContentStatus contentStatus = course.getContentStatus();
        ApprovalStatus approvalStatus = course.getApprovalStatus();
        if (!ApprovalStatus.PENDING.equals(approvalStatus)) {
            throw new RuntimeException("Course is not requested for review");  
        }

        if (ContentStatus.PUBLISHED_EDITING.equals(contentStatus)) {
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
        emailService.sendApprovalEmail(course.getInstructor(), course);
    }

    @Transactional
    public void rejectCourse(Long courseId, String rejectionReason) throws RuntimeException {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        ContentStatus contentStatus = course.getContentStatus();
        ApprovalStatus approvalStatus = course.getApprovalStatus();
        if (!ApprovalStatus.PENDING.equals(approvalStatus)) {
            throw new RuntimeException("Course is not requested for review");  
        }

        if (ContentStatus.READY_TO_REVIEW.equals(contentStatus)) {
            course.setContentStatus(ContentStatus.DRAFT);
        } else if (ContentStatus.PUBLISHED_EDITING.equals(contentStatus)) {
            course.setContentStatus(ContentStatus.PUBLISHED);
        }

        course.setApprovalStatus(ApprovalStatus.REJECTED);
        course.setRejectReason(rejectionReason);
        courseRepository.save(course);

        // Send notification email
        emailService.sendRejectionEmail(course.getInstructor(), course, rejectionReason);
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