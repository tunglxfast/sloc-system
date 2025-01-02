package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.CategoryDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.entity.*;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.mapper.CategoryMapper;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.mapper.UserMapper;
import funix.sloc_system.repository.CategoryRepository;
import funix.sloc_system.repository.ContentChangeRepository;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.UserRepository;
import funix.sloc_system.util.AppUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
public class CourseService {
    public final String THUMBNAIL_LOCAL = "/img/courses/thumbnails/";
    private boolean isCheckedOnStartup = false;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ContentChangeRepository contentChangeRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private AppUtil appUtil;

    /**
     * Run when start app
     */
    @PostConstruct
    public void checkOnStartup() {
        // check to make sure not calling second time after startup
        if (!isCheckedOnStartup) {
            // check Expired Courses
            archiveExpiredCourses();
            // Evaluate study process
//            evaluateStudyProcess();
            isCheckedOnStartup = true;
        }
    }

    /**
     * Run in midnight and turn course to archived when out of learning time
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void archiveExpiredCourses() {
        LocalDate now = LocalDate.now();
        Set<Course> courses = courseRepository.findByEndDateBeforeAndContentStatusNot(now, ContentStatus.ARCHIVED);

        for (Course course : courses) {
            course.setContentStatus(ContentStatus.ARCHIVED);
            courseRepository.save(course);
        }
    }

    /**
     * Run in weekly at 1 am on Monday to evaluate students study process.
     * Courses have to be published and total learning day longer than 14 days.
     */
    @Scheduled(cron = "0 0 1 * * MON")
    public void evaluateStudyProcess() {
        List<ContentStatus> contentStatusList = List.of(
                ContentStatus.PUBLISHED,
                ContentStatus.PUBLISHED_EDITING);
        List<Course> courses = courseRepository.findByContentStatusIn(contentStatusList);

        for (Course course : courses) {
            long duration = course.getEndDate().toEpochDay() - course.getStartDate().toEpochDay();
            if (duration < 14) {
                courses.remove(course);
            }
        }

        for (Course course : courses) {
            appUtil.evaluateStudentsStudy(course.getId());
        }
    }

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

    /**
     * Course have to be published.
     * @return
     */
    public List<Course> getAvailableCourses() {
        List<ContentStatus> contentStatusList = List.of(
                ContentStatus.PUBLISHED,
                ContentStatus.PUBLISHED_EDITING);
        return courseRepository.findByContentStatusIn(contentStatusList);
    }

    public List<Course> findCoursesByTitle(String title) {
        return courseRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Course> findCoursesByCategory(String category) {
        Category categoryEntity = categoryRepository.findByNameIgnoreCase(category).orElse(null);
        if (categoryEntity == null) {
            return new ArrayList<>();
        }
        return courseRepository.findByCategory(categoryEntity);
    }

    public List<Course> findCoursesByTitleAndCategory(String title, String category) {
        Category categoryEntity = categoryRepository.findByNameIgnoreCase(category).orElse(null); 
        if (categoryEntity == null) {
            return new ArrayList<>();
        }
        return courseRepository.findByTitleContainingIgnoreCaseAndCategory(title, categoryEntity);
    }

    @Transactional
    public void submitForReview(Long courseId) throws IllegalArgumentException {
        Course course = findById(courseId);

        // change course content status for reviewing
        if (course == null) {
            throw new IllegalArgumentException("Course not found!");
        } else if (course.getChapters().isEmpty()) {
            throw new IllegalArgumentException("Course must have at least one chapter before submit for review.");
        }

        if (course.getContentStatus() == ContentStatus.DRAFT){
            course.setContentStatus(ContentStatus.READY_TO_REVIEW);
            course.setApprovalStatus(ApprovalStatus.PENDING);
        } else {
            course.setContentStatus(ContentStatus.PUBLISHED_EDITING);
            course.setApprovalStatus(ApprovalStatus.PENDING);
        }
        courseRepository.save(course);
    }

    @Transactional
    public List<Course> findByInstructor(User instructor) {
        return courseRepository.findByInstructor(instructor);
    }

    /**
     * Save course basic datas to database.
     * @param courseDTO
     * @param instructorId
     * @param file
     * @param categoryId
     * @return
     * @throws IOException
     */
    @Transactional
    public CourseDTO createNewCourse(CourseDTO courseDTO,
                                     Long instructorId,
                                     MultipartFile file,
                                     Long categoryId) throws IOException {

        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Missing user information, please login again."));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category information error, please contact support center."));

        UserDTO instructorDTO = userMapper.toDTO(instructor);
        CategoryDTO categoryDTO = categoryMapper.toDTO(category);

        courseDTO.setContentStatus(ContentStatus.DRAFT.name());
        courseDTO.setInstructor(instructorDTO);
        courseDTO.setCreatedAt(LocalDate.now());
        courseDTO.setCreatedBy(instructorDTO);
        courseDTO.setCategory(categoryDTO);

        String thumbnailUrl = saveThumbnail(file);
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()){
            courseDTO.setThumbnailUrl(thumbnailUrl);
        }

        Course course = courseMapper.toEntity(courseDTO, instructor);
        courseRepository.save(course);
        return courseMapper.toDTO(course);
    }

    /**
     * Update course based on content status:
     * - If course is not published (DRAFT): save directly to main table
     * - If course is published and editing existing content: save to temp table
     */
    @Transactional
    public void updateCourse(Long courseId, CourseDTO editingValues, Long instructorId, MultipartFile file, Long categoryId) throws Exception {
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);
        User instructor = userRepository.findById(instructorId).orElse(null);
        Category category = categoryRepository.findById(categoryId).orElse(null);

        // update title, description, category, start/end date, update time and updater
        UserDTO instructorDTO = userMapper.toDTO(instructor);
        CategoryDTO categoryDTO = categoryMapper.toDTO(category);
        courseDTO.updateEditingValues(editingValues, categoryDTO, instructorDTO);

        // Update thumbnail if provided
        if (file != null && !file.isEmpty()) {
            String thumbnailPath = saveThumbnail(file);
            courseDTO.setThumbnailUrl(thumbnailPath);
        }

        String currentStatus = courseDTO.getContentStatus();
        // 1. Course is in DRAFT state (not published yet)
        if (ContentStatus.DRAFT.name().equals(currentStatus)) {
            // save direct to table
            courseRepository.save(courseMapper.toEntity(courseDTO, instructor));
        } else {
            // Save course changes (json type) to temp table.
            String json = objectMapper.writeValueAsString(courseDTO);
            appUtil.saveContentChange(json, courseId, instructorId, ContentAction.UPDATE);
        }
    }

    private String saveThumbnail(MultipartFile file) throws NullPointerException, IOException {
        String uuid = UUID.randomUUID().toString();
        if (!file.isEmpty()) {
            String fileName = String.format("thumbnail-%s.jpg", uuid);
            String absolutePath = Paths.get("").toAbsolutePath() + "/src/main/resources/static" + THUMBNAIL_LOCAL;
            File saveFile = new File(absolutePath + fileName);
            file.transferTo(saveFile);
            return "/img/courses/thumbnails/" + fileName;
        } else {
            return null;
        }
    }

    public boolean checkCourseExists(Long courseId) {
        return courseRepository.existsById(courseId);
    }

    /**
     * Validate if an entity can be modified
     * throw exception if not allow modify
     */
    private void checkCourseAllowModify(Course course, Long instructorId) {
        if (course.getContentStatus() == ContentStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot modify archived course content");
        }
        if (course.getApprovalStatus() == ApprovalStatus.PENDING) {
            throw new IllegalStateException("Cannot modify content while approval is pending");
        }
        if (course.getInstructor() != null && course.getInstructor().getId() != instructorId) {
            throw new IllegalStateException("Only instructor of this course can modify content");
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

    @Transactional
    public String deleteCourse(Long courseId, Long instructorId) throws Exception {
        Course course = findById(courseId);
        if (course != null) {
            if (course.getContentStatus() == ContentStatus.DRAFT) {
                courseRepository.deleteById(courseId);
                return "Course deleted successfully.";
            } else {
                String json;
                try {
                    CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);
                    json = objectMapper.writeValueAsString(courseDTO);
                } catch (Exception e) {
                    throw new Exception("Error when deleting course content");
                }
                appUtil.saveContentChange(json, courseId, instructorId, ContentAction.DELETE);

                submitForReview(courseId);
                return "Delete course request is sent, please wait for approval.";
            }
        }
        return "Course not found.";
    }

    public void resetCourse(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (ApprovalStatus.PENDING.equals(course.getApprovalStatus())) {
            throw new IllegalStateException("Cannot reset course while review is pending");
        }

        ContentChangeTemporary contentChange = contentChangeRepository.findByEntityTypeAndEntityId(EntityType.COURSE, courseId).orElse(null);
        if (contentChange != null) {
            contentChangeRepository.delete(contentChange);
        }
    }
}
