package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.enums.*;
import funix.sloc_system.repository.*;
import funix.sloc_system.dto.*;
import funix.sloc_system.entity.*;
import funix.sloc_system.mapper.*;
import funix.sloc_system.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ContentChangeRepository contentChangeRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private ChapterMapper chapterMapper;
    @Autowired
    private TopicMapper topicMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ApplicationUtil appUtil;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course findById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public List<Course> findByCategoryId(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId);
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public List<Course> getInstructorRejectedCourses(User instructor) {
        return courseRepository.findAllByInstructorAndApprovalStatus(instructor, ApprovalStatus.REJECTED);
    }

    public List<CourseDTO> getAvailableCourseDTOList() {
        List<ContentStatus> contentStatusList = List.of(
                ContentStatus.PUBLISHED,
                ContentStatus.PUBLISHED_EDITING,
                ContentStatus.ARCHIVED);
        List<Course> courses = courseRepository.findByContentStatusIn(contentStatusList);
        return courseMapper.toDTO(courses);
    }

    @Transactional
    public void submitForCreatingReview(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getChapters().isEmpty()) {
            throw new IllegalArgumentException("Course must have at least one chapter before submission.");
        }

        // Update course status
        course.setContentStatus(ContentStatus.READY_TO_REVIEW);
        course.setApprovalStatus(ApprovalStatus.PENDING);

        // Update child entities status
        for (Chapter chapter : course.getChapters()) {
            chapter.setContentStatus(ContentStatus.READY_TO_REVIEW);
            for (Topic topic : chapter.getTopics()) {
                topic.setContentStatus(ContentStatus.READY_TO_REVIEW);
                if (topic.getQuestions() != null) {
                    for (Question question : topic.getQuestions()) {
                        question.setContentStatus(ContentStatus.READY_TO_REVIEW);
                        if (question.getAnswers() != null) {
                            question.getAnswers().forEach(answer -> 
                                answer.setContentStatus(ContentStatus.READY_TO_REVIEW));
                        }
                    }
                }
            }
        }

        courseRepository.save(course);
    }

    @Transactional
    public void submitForEditingReview(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // Update course status
        course.setContentStatus(ContentStatus.PUBLISHED_EDITING);
        course.setApprovalStatus(ApprovalStatus.PENDING);

        // Get all changes from temporary table
        List<ContentChangeTemporary> allChanges = getAllCourseChanges(course);

        // Update child entities status based on their change action
        for (Chapter chapter : course.getChapters()) {
            Optional<ContentChangeTemporary> chapterChange = allChanges.stream()
                .filter(change -> change.getEntityType() == EntityType.CHAPTER 
                    && change.getEntityId().equals(chapter.getId()))
                .findFirst();

            if (chapterChange.isPresent()) {
                ContentAction action = chapterChange.get().getAction();
                if (action == ContentAction.CREATE) {
                    chapter.setContentStatus(ContentStatus.READY_TO_REVIEW);
                } else {
                    // Both UPDATE and DELETE should change to PUBLISHED_EDITING
                    chapter.setContentStatus(ContentStatus.PUBLISHED_EDITING);
                }
            }

            for (Topic topic : chapter.getTopics()) {
                Optional<ContentChangeTemporary> topicChange = allChanges.stream()
                    .filter(change -> change.getEntityType() == EntityType.TOPIC 
                        && change.getEntityId().equals(topic.getId()))
                    .findFirst();

                if (topicChange.isPresent()) {
                    ContentAction action = topicChange.get().getAction();
                    if (action == ContentAction.CREATE) {
                        topic.setContentStatus(ContentStatus.READY_TO_REVIEW);
                    } else {
                        // Both UPDATE and DELETE should change to PUBLISHED_EDITING
                        topic.setContentStatus(ContentStatus.PUBLISHED_EDITING);
                    }
                }

                if (topic.getQuestions() != null) {
                    for (Question question : topic.getQuestions()) {
                        Optional<ContentChangeTemporary> questionChange = allChanges.stream()
                            .filter(change -> change.getEntityType() == EntityType.QUESTION 
                                && change.getEntityId().equals(question.getId()))
                            .findFirst();

                        if (questionChange.isPresent()) {
                            ContentAction action = questionChange.get().getAction();
                            if (action == ContentAction.CREATE) {
                                question.setContentStatus(ContentStatus.READY_TO_REVIEW);
                                if (question.getAnswers() != null) {
                                    question.getAnswers().forEach(answer -> 
                                        answer.setContentStatus(ContentStatus.READY_TO_REVIEW));
                                }
                            } else {
                                // Both UPDATE and DELETE should change to PUBLISHED_EDITING
                                question.setContentStatus(ContentStatus.PUBLISHED_EDITING);
                                if (question.getAnswers() != null) {
                                    question.getAnswers().forEach(answer -> 
                                        answer.setContentStatus(ContentStatus.PUBLISHED_EDITING));
                                }
                            }
                        }
                    }
                }
            }
        }

        courseRepository.save(course);
    }

    @Transactional
    public void submitForReview(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // change course content status for reviewing
        if (course.getContentStatus() == ContentStatus.DRAFT){
            submitForCreatingReview(courseId);
        } else {
            submitForEditingReview(courseId);
        }
    }

    @Transactional
    public void approveCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // If this is a new course or editing an existing course
        if (course.getContentStatus() == ContentStatus.READY_TO_REVIEW) {
            // Update course and all child entities to PUBLISHED
            updateContentStatusRecursively(course, ContentStatus.PUBLISHED);
        } else if (course.getContentStatus() == ContentStatus.PUBLISHED_EDITING) {
            // Apply changes from temporary table to main table
            applyTemporaryChanges(course);
            // Update course and modified child entities to PUBLISHED
            updateContentStatusRecursively(course, ContentStatus.PUBLISHED);
        }

        course.setApprovalStatus(ApprovalStatus.APPROVED);
        course.setRejectReason(null);
        courseRepository.save(course);

        // Clean up temporary changes
        deleteAllCourseChanges(course);

        // Send notification
        sendApproveEmail(course.getInstructor(), course);
    }

    @Transactional
    public void rejectCourse(Long courseId, String reason) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getContentStatus() == ContentStatus.READY_TO_REVIEW) {
            // Update course and all child entities to DRAFT
            updateContentStatusRecursively(course, ContentStatus.DRAFT);
        } else if (course.getContentStatus() == ContentStatus.PUBLISHED_EDITING) {
            // Update course and modified child entities to PUBLISHED
            updateContentStatusRecursively(course, ContentStatus.PUBLISHED);
        }

        course.setApprovalStatus(ApprovalStatus.REJECTED);
        course.setRejectReason(reason);
        courseRepository.save(course);

        // Send notification
        sendRejectionEmail(course.getInstructor(), course, reason);
    }

    /**
     * Recursively update content status of course and its child entities
     */
    private void updateContentStatusRecursively(Course course, ContentStatus newStatus) {
        course.setContentStatus(newStatus);
        
        for (Chapter chapter : course.getChapters()) {
            // For PUBLISHED_EDITING, only update entities that have changes
            if (newStatus == ContentStatus.PUBLISHED && course.getContentStatus() == ContentStatus.PUBLISHED_EDITING) {
                if (contentChangeRepository.findByEntityTypeAndEntityId(EntityType.CHAPTER, chapter.getId()).isPresent()) {
                    chapter.setContentStatus(newStatus);
                }
            } else {
                chapter.setContentStatus(newStatus);
            }

            for (Topic topic : chapter.getTopics()) {
                if (newStatus == ContentStatus.PUBLISHED && course.getContentStatus() == ContentStatus.PUBLISHED_EDITING) {
                    if (contentChangeRepository.findByEntityTypeAndEntityId(EntityType.TOPIC, topic.getId()).isPresent()) {
                        topic.setContentStatus(newStatus);
                    }
                } else {
                    topic.setContentStatus(newStatus);
                }

                if (topic.getQuestions() != null) {
                    for (Question question : topic.getQuestions()) {
                        if (newStatus == ContentStatus.PUBLISHED && course.getContentStatus() == ContentStatus.PUBLISHED_EDITING) {
                            if (contentChangeRepository.findByEntityTypeAndEntityId(EntityType.QUESTION, question.getId()).isPresent()) {
                                question.setContentStatus(newStatus);
                                if (question.getAnswers() != null) {
                                    question.getAnswers().forEach(answer -> answer.setContentStatus(newStatus));
                                }
                            }
                        } else {
                            question.setContentStatus(newStatus);
                            if (question.getAnswers() != null) {
                                question.getAnswers().forEach(answer -> answer.setContentStatus(newStatus));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Apply changes from temporary table to main entities
     */
    private void applyTemporaryChanges(Course course) {
        // Get all changes for this course and its child entities
        List<ContentChangeTemporary> allChanges = getAllCourseChanges(course);

        // First handle deletions to avoid foreign key conflicts
        for (ContentChangeTemporary change : allChanges) {
            if (change.getAction() == ContentAction.DELETE) {
                switch (change.getEntityType()) {
                    case CHAPTER:
                        chapterRepository.deleteById(change.getEntityId());
                        break;
                    case TOPIC:
                        topicRepository.deleteById(change.getEntityId());
                        break;
                    case QUESTION:
                        questionRepository.deleteById(change.getEntityId());
                        break;
                }
            }
        }

        // Then handle updates and creations
        for (ContentChangeTemporary change : allChanges) {
            if (change.getAction() != ContentAction.DELETE) {
                try {
                    switch (change.getEntityType()) {
                        case COURSE:
                            CourseDTO courseDTO = objectMapper.readValue(change.getChanges(), CourseDTO.class);
                            Course updatedCourse = courseMapper.toEntity(courseDTO);
                            // Maintain relationships
                            updatedCourse.setInstructor(course.getInstructor());
                            updatedCourse.setCreatedBy(course.getCreatedBy());
                            courseRepository.save(updatedCourse);
                            break;

                        case CHAPTER:
                            ChapterDTO chapterDTO = objectMapper.readValue(change.getChanges(), ChapterDTO.class);
                            Chapter updatedChapter = chapterMapper.toEntity(chapterDTO);
                            // Maintain relationship with course
                            updatedChapter.setCourse(course);
                            chapterRepository.save(updatedChapter);
                            break;

                        case TOPIC:
                            TopicDTO topicDTO = objectMapper.readValue(change.getChanges(), TopicDTO.class);
                            Topic updatedTopic = topicMapper.toEntity(topicDTO);
                            // Find parent chapter from DTO
                            Chapter parentChapter = chapterRepository.findById(topicDTO.getChapterId())
                                .orElseThrow(() -> new RuntimeException("Parent chapter not found"));
                            // Maintain relationship with chapter
                            updatedTopic.setChapter(parentChapter);
                            topicRepository.save(updatedTopic);
                            break;

                        case QUESTION:
                            QuestionDTO questionDTO = objectMapper.readValue(change.getChanges(), QuestionDTO.class);
                            Question updatedQuestion = questionMapper.toEntity(questionDTO);
                            // Find parent topic from DTO
                            Topic parentTopic = topicRepository.findById(questionDTO.getTopicId())
                                .orElseThrow(() -> new RuntimeException("Parent topic not found"));
                            // Maintain relationship with topic
                            updatedQuestion.setTopic(parentTopic);
                            // Maintain relationships for answers
                            if (updatedQuestion.getAnswers() != null) {
                                updatedQuestion.getAnswers().forEach(answer -> answer.setQuestion(updatedQuestion));
                            }
                            questionRepository.save(updatedQuestion);
                            break;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to apply changes for " + change.getEntityType(), e);
                }
            }
        }
    }

    /**
     * Get editing changes for any entity type
     */
    @Transactional
    public <T> T getEditingEntityDTO(EntityType entityType, Long id, Class<T> dtoClass) throws Exception {
        ContentChangeTemporary changeTemporary = contentChangeRepository.findByEntityTypeAndEntityId(
                entityType,
                id).orElse(null);
        if (changeTemporary == null) {
            return null;
        } else {
            String changeContext = changeTemporary.getChanges();
            return objectMapper.readValue(changeContext, dtoClass);
        }
    }

    @Transactional
    public CourseDTO getEditingCourseDTO(Long id) throws Exception {
        Course course = findById(id);
        CourseDTO editingCourse = getEditingEntityDTO(EntityType.COURSE, id, CourseDTO.class);
        return editingCourse != null ? editingCourse : courseMapper.toDTO(course);
    }

    public void sendRejectionEmail(User instructor, Course course, String reason) {
        String subject = String.format("Course Rejected: %s", course.getTitle());
        String body = String.format(
                "Dear %s,%n%n"
                + "Your course '%s' has been rejected.%n"
                + "Reason: %s %n%n"
                + "Please review and make necessary changes.%n"
                + "Thank you.",
                instructor.getFullName(), course.getTitle(), reason
        );
        emailService.sendEmail(instructor.getEmail(), subject, body);
    }

    public void sendApproveEmail(User instructor, Course course) {
        String subject = "Course approve: " + course.getTitle();
        String body = String.format(
                "Dear %s,%n%n"
                + "Your course '%s'"
                + "has been approved.%n"
                + "Thank you.",
                instructor.getFullName(), course.getTitle());
        emailService.sendEmail(instructor.getEmail(), subject, body);
    }

    @Transactional
    public List<Course> findByInstructor(User instructor) {
        return courseRepository.findByInstructor(instructor);
    }

    @Transactional
    public CourseDTO createNewCourse(CourseDTO courseDTO,
                                     Long instructorId,
                                     MultipartFile file,
                                     Long categoryId) throws IOException {

        User instructor = userRepository.findById(instructorId).orElse(null);
        Category category = categoryRepository.findById(categoryId).orElse(null);
        courseDTO.setContentStatus(ContentStatus.DRAFT.name());
        courseDTO.setInstructor(instructor);
        courseDTO.setCreatedAt(LocalDate.now());
        courseDTO.setCreatedBy(instructor);
        courseDTO.setCategory(category);

        String thumbnailUrl = saveThumbnail(file);
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()){
            courseDTO.setThumbnailUrl(thumbnailUrl);
        }

        Course course = courseMapper.toEntity(courseDTO);
        courseRepository.save(course);
        return courseMapper.toDTO(course);
    }

    /**
     * Update course based on content status:
     * - If course is not published (DRAFT): save directly to main table
     * - If course is published and editing existing content: save to temp table
     */
    @Transactional
    public void saveUpdateCourse(Long courseId, CourseDTO courseDTO, Long instructorId, MultipartFile file, Long categoryId) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        
        User instructor = userRepository.findById(instructorId).orElse(null);
        Category category = categoryRepository.findById(categoryId).orElse(null);
        courseDTO.setInstructor(instructor);
        courseDTO.setCategory(category);
        courseDTO.setUpdatedAt(LocalDate.now());
        courseDTO.setLastUpdatedBy(instructor);
        courseDTO.setCategory(category);
        // Handle thumbnail update if provided
        if (file != null && !file.isEmpty()) {
            String thumbnailPath = saveThumbnail(file);
            courseDTO.setThumbnailUrl(thumbnailPath);
        }
        
        ContentStatus currentStatus = course.getContentStatus();
        // 1. Course is in DRAFT state (not published yet)
        if (currentStatus == ContentStatus.DRAFT) {
            // Update course using entity method
            Course updatedCourse = courseMapper.toEntity(courseDTO);
            course.updateWithOtherCourse(updatedCourse);
            courseRepository.save(course);
        } else {
            // Save course changes to temp table.
            appUtil.saveEntityChanges(EntityType.COURSE, courseDTO, course, ContentAction.UPDATE, instructorId);
        }
    }

    @Transactional
    private String saveThumbnail(MultipartFile file) throws NullPointerException, IOException {
        String uuid = UUID.randomUUID().toString();
        if (!file.isEmpty()) {
            String fileName = String.format("thumbnail-%s.jpg", uuid);
            String absolutePath = Paths.get("").toAbsolutePath() + "/src/main/resources/static/img/";
            File saveFile = new File(absolutePath + fileName);
            file.transferTo(saveFile);
            return "/img/" + fileName;
        } else {
            return null;
        }
    }

    public boolean courseExists(Long courseId) {
        return courseRepository.existsById(courseId);
    }

    /**
     * Validate if an entity can be modified
     */
    private void validateEntityModification(Course course) {
        if (course.getContentStatus() == ContentStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot modify archived course content");
        }
        if (course.getApprovalStatus() == ApprovalStatus.PENDING) {
            throw new IllegalStateException("Cannot modify content while approval is pending");
        }
    }

    /**
     * Check if course has any pending changes
     */
    @Transactional
    public boolean hasPendingChanges(Course course) {
        // Check course changes
        if (contentChangeRepository.findByEntityTypeAndEntityId(EntityType.COURSE, course.getId()).isPresent()) {
            return true;
        }

        // Check chapter changes
        for (Chapter chapter : course.getChapters()) {
            if (contentChangeRepository.findByEntityTypeAndEntityId(EntityType.CHAPTER, chapter.getId()).isPresent()) {
                return true;
            }

            // Check topic changes
            for (Topic topic : chapter.getTopics()) {
                if (contentChangeRepository.findByEntityTypeAndEntityId(EntityType.TOPIC, topic.getId()).isPresent()) {
                    return true;
                }

                // Check question changes
                if (topic.getQuestions() != null) {
                    for (Question question : topic.getQuestions()) {
                        if (contentChangeRepository.findByEntityTypeAndEntityId(EntityType.QUESTION, question.getId()).isPresent()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get all changes for a course and its child entities
     */
    @Transactional
    public List<ContentChangeTemporary> getAllCourseChanges(Course course) {
        List<ContentChangeTemporary> allChanges = new ArrayList<>();

        // Get course changes
        Optional<ContentChangeTemporary> courseChange = contentChangeRepository.findByEntityTypeAndEntityId(EntityType.COURSE, course.getId());
        courseChange.ifPresent(allChanges::add);

        // Get chapter changes
        for (Chapter chapter : course.getChapters()) {
            Optional<ContentChangeTemporary> chapterChange = contentChangeRepository.findByEntityTypeAndEntityId(EntityType.CHAPTER, chapter.getId());
            chapterChange.ifPresent(allChanges::add);

            // Get topic changes
            for (Topic topic : chapter.getTopics()) {
                Optional<ContentChangeTemporary> topicChange = contentChangeRepository.findByEntityTypeAndEntityId(EntityType.TOPIC, topic.getId());
                topicChange.ifPresent(allChanges::add);

                // Get question changes
                if (topic.getQuestions() != null) {
                    for (Question question : topic.getQuestions()) {
                        Optional<ContentChangeTemporary> questionChange = contentChangeRepository.findByEntityTypeAndEntityId(EntityType.QUESTION, question.getId());
                        questionChange.ifPresent(allChanges::add);
                    }
                }
            }
        }

        return allChanges;
    }

    /**
     * Delete all changes related to a course when it's approved.
     * This includes changes to the course itself, chapters, topics, and questions (with their answers).
     */
    @Transactional
    public void deleteAllCourseChanges(Course course) {
        // Delete course changes
        contentChangeRepository.deleteByEntityTypeAndEntityId(EntityType.COURSE, course.getId());

        // Delete chapter changes
        for (Chapter chapter : course.getChapters()) {
            contentChangeRepository.deleteByEntityTypeAndEntityId(EntityType.CHAPTER, chapter.getId());

            // Delete topic changes
            for (Topic topic : chapter.getTopics()) {
                contentChangeRepository.deleteByEntityTypeAndEntityId(EntityType.TOPIC, topic.getId());

                // Delete question changes (answers are included in question JSON)
                if (topic.getQuestions() != null) {
                    for (Question question : topic.getQuestions()) {
                        contentChangeRepository.deleteByEntityTypeAndEntityId(EntityType.QUESTION, question.getId());
                    }
                }
            }
        }
    }
}
