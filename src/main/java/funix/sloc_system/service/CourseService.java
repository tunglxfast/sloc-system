package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.enums.*;
import funix.sloc_system.repository.*;
import funix.sloc_system.dto.*;
import funix.sloc_system.entity.*;
import funix.sloc_system.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;
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
    private ContentChangeService contentChangeService;
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

        course.setContentStatus(ContentStatus.READY_TO_REVIEW);
        course.setApprovalStatus(ApprovalStatus.PENDING);
        courseRepository.save(course);
    }

    @Transactional
    public void submitForEditingReview(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        course.setContentStatus(ContentStatus.PUBLISHED_EDITING);
        course.setApprovalStatus(ApprovalStatus.PENDING);
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
            course.setContentStatus(ContentStatus.PUBLISHED);
        } else if (course.getContentStatus() == ContentStatus.PUBLISHED_EDITING) {
            // Apply changes from temporary table to main table
            applyTemporaryChanges(course);
            course.setContentStatus(ContentStatus.PUBLISHED);
        }

        course.setApprovalStatus(ApprovalStatus.APPROVED);
        course.setRejectReason(null);
        courseRepository.save(course);

        // Clean up temporary changes
        contentChangeService.deleteAllCourseChanges(course);

        // Send notification
        sendApproveEmail(course.getInstructor(), course);
    }

    @Transactional
    public void rejectCourse(Long courseId, String reason) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getContentStatus() == ContentStatus.READY_TO_REVIEW) {
            course.setContentStatus(ContentStatus.DRAFT);
        } else if (course.getContentStatus() == ContentStatus.PUBLISHED_EDITING) {
            course.setContentStatus(ContentStatus.PUBLISHED);
        }

        course.setApprovalStatus(ApprovalStatus.REJECTED);
        course.setRejectReason(reason);
        courseRepository.save(course);

        // Send notification
        sendRejectionEmail(course.getInstructor(), course, reason);
    }

    /**
     * Apply changes from temporary table to main entities
     */
    private void applyTemporaryChanges(Course course) {
        // Apply course changes
        Optional<ContentChangeTemporary> courseChanges = contentChangeService.getEntityEditing(
                EntityType.COURSE, course.getId());
        
        if (courseChanges.isPresent()) {
            try {
                CourseDTO courseDTO = objectMapper.readValue(courseChanges.get().getChanges(), CourseDTO.class);
                Course updatedCourse = courseMapper.toEntity(courseDTO);
                // Maintain relationships
                updatedCourse.setInstructor(course.getInstructor());
                updatedCourse.setCreatedBy(course.getCreatedBy());
                courseRepository.save(updatedCourse);
            } catch (Exception e) {
                throw new RuntimeException("Failed to apply course changes", e);
            }
        }

        // Apply chapter changes
        for (Chapter chapter : course.getChapters()) {
            Optional<ContentChangeTemporary> chapterChanges = contentChangeService.getEntityEditing(
                    EntityType.CHAPTER, chapter.getId());
            
            if (chapterChanges.isPresent()) {
                try {
                    ChapterDTO chapterDTO = objectMapper.readValue(chapterChanges.get().getChanges(), ChapterDTO.class);
                    Chapter updatedChapter = chapterMapper.toEntity(chapterDTO);
                    // Maintain relationship with course
                    updatedChapter.setCourse(course);
                    chapterRepository.save(updatedChapter);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to apply chapter changes", e);
                }
            }

            // Apply topic changes
            for (Topic topic : chapter.getTopics()) {
                Optional<ContentChangeTemporary> topicChanges = contentChangeService.getEntityEditing(
                        EntityType.TOPIC, topic.getId());
                
                if (topicChanges.isPresent()) {
                    try {
                        TopicDTO topicDTO = objectMapper.readValue(topicChanges.get().getChanges(), TopicDTO.class);
                        Topic updatedTopic = topicMapper.toEntity(topicDTO);
                        // Maintain relationship with chapter
                        updatedTopic.setChapter(chapter);
                        topicRepository.save(updatedTopic);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to apply topic changes", e);
                    }
                }

                // Apply question changes (including answers)
                if (topic.getQuestions() != null) {
                    for (Question question : topic.getQuestions()) {
                        Optional<ContentChangeTemporary> questionChanges = contentChangeService.getEntityEditing(
                                EntityType.QUESTION, question.getId());
                        
                        if (questionChanges.isPresent()) {
                            try {
                                QuestionDTO questionDTO = objectMapper.readValue(questionChanges.get().getChanges(), QuestionDTO.class);
                                Question updatedQuestion = questionMapper.toEntity(questionDTO);
                                // Maintain relationship with topic
                                updatedQuestion.setTopic(topic);
                                // Maintain relationships for answers
                                if (updatedQuestion.getAnswers() != null) {
                                    updatedQuestion.getAnswers().forEach(answer -> answer.setQuestion(updatedQuestion));
                                }
                                questionRepository.save(updatedQuestion);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to apply question changes", e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get editing changes for any entity type
     */
    @Transactional
    public <T> T getEditingEntityDTO(EntityType entityType, Long id, Class<T> dtoClass) throws Exception {
        ContentChangeTemporary changeTemporary = contentChangeService.getEntityEditing(
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
     * - If course is not published (DRAFT) or new chapters/topics are added to published course: save directly to main table
     * - If course is published and editing existing content: save to temp table
     */
    @Transactional
    public void saveUpdateCourse(CourseDTO courseDTO, Long instructorId, MultipartFile file, Long categoryId) throws IOException {
        User instructor = userRepository.findById(instructorId).orElse(null);
        Category category = categoryRepository.findById(categoryId).orElse(null);
        courseDTO.setInstructor(instructor);
        courseDTO.setCategory(category);

        String thumbnailUrl = saveThumbnail(file);
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()){
            courseDTO.setThumbnailUrl(thumbnailUrl);
        }
        courseDTO.setUpdatedAt(LocalDate.now());
        courseDTO.setLastUpdatedBy(instructor);

        Course existingCourse = findById(courseDTO.getId());
        ContentStatus currentStatus = existingCourse != null ? existingCourse.getContentStatus() : ContentStatus.DRAFT;

        // Save directly to main table if:
        // 1. Course is in DRAFT state (not published yet)
        // 2. New chapters/topics are being added to a published course
        if (currentStatus == ContentStatus.DRAFT) {
            Course course = courseMapper.toEntity(courseDTO);
            courseRepository.save(course);
        } else {
            // For published courses, check if we're adding new content or modifying existing
            boolean hasNewContent = hasNewChaptersOrTopics(courseDTO, existingCourse);
            
            if (hasNewContent) {
                // New chapters/topics go directly to main table
                Course course = courseMapper.toEntity(courseDTO);
                courseRepository.save(course);
            } else {
                // Modifications to existing content go to temp table
                contentChangeService.saveEditingCourse(
                        courseDTO,
                        ContentAction.UPDATE,
                        instructorId);
            }
        }
    }

    /**
     * Check if the courseDTO contains new chapters or topics compared to existing course
     */
    private boolean hasNewChaptersOrTopics(CourseDTO courseDTO, Course existingCourse) {
        if (courseDTO.getChapters() == null) {
            return false;
        }

        // Check for new chapters
        Set<Long> existingChapterIds = existingCourse.getChapters().stream()
                .map(Chapter::getId)
                .collect(Collectors.toSet());

        boolean hasNewChapters = courseDTO.getChapters().stream()
                .anyMatch(chapterDTO -> chapterDTO.getId() == null || !existingChapterIds.contains(chapterDTO.getId()));

        if (hasNewChapters) {
            return true;
        }

        // Check for new topics in existing chapters
        for (ChapterDTO chapterDTO : courseDTO.getChapters()) {
            if (chapterDTO.getId() != null) {
                Chapter existingChapter = existingCourse.getChapters().stream()
                        .filter(ch -> ch.getId().equals(chapterDTO.getId()))
                        .findFirst()
                        .orElse(null);

                if (existingChapter != null && chapterDTO.getTopics() != null) {
                    Set<Long> existingTopicIds = existingChapter.getTopics().stream()
                            .map(Topic::getId)
                            .collect(Collectors.toSet());

                    boolean hasNewTopics = chapterDTO.getTopics().stream()
                            .anyMatch(topicDTO -> topicDTO.getId() == null || !existingTopicIds.contains(topicDTO.getId()));

                    if (hasNewTopics) {
                        return true;
                    }
                }
            }
        }

        return false;
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
        if (contentChangeService.getEntityEditing(EntityType.COURSE, course.getId()).isPresent()) {
            return true;
        }

        // Check chapter changes
        for (Chapter chapter : course.getChapters()) {
            if (contentChangeService.getEntityEditing(EntityType.CHAPTER, chapter.getId()).isPresent()) {
                return true;
            }

            // Check topic changes
            for (Topic topic : chapter.getTopics()) {
                if (contentChangeService.getEntityEditing(EntityType.TOPIC, topic.getId()).isPresent()) {
                    return true;
                }

                // Check question changes
                if (topic.getQuestions() != null) {
                    for (Question question : topic.getQuestions()) {
                        if (contentChangeService.getEntityEditing(EntityType.QUESTION, question.getId()).isPresent()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
