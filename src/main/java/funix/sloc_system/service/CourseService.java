package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.UserRepository;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.Category;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.CourseChangeTemporary;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.CourseChangeAction;
import funix.sloc_system.enums.CourseStatus;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.repository.CategoryRepository;
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

@Service
public class CourseService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ContentChangeService contentChangeService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CourseMapper courseMapper;

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

    public List<Course> findAllByInstructorAndStatus(User instructor, CourseStatus status) {
        return courseRepository.findAllByInstructorAndStatus(instructor, status);
    }

    public List<Course> getApprovedOrUpdatingCourses() {
        return courseRepository.findByStatusIn(List.of(CourseStatus.APPROVED, CourseStatus.PENDING_EDIT));
    }

    public void submitForCreatingReview(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getChapters().isEmpty()) {
            throw new IllegalArgumentException("Course must have at least one chapter before submission.");
        }

        course.setStatus(CourseStatus.PENDING_CREATE);
        courseRepository.save(course);
    }

    public void submitForReview(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // change course status for reviewing
        if (course.getStatus() == CourseStatus.DRAFT){
            course.setStatus(CourseStatus.PENDING_CREATE);
        } else {
            course.setStatus(CourseStatus.PENDING_EDIT);
        }
        courseRepository.save(course);
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
        courseDTO.setStatus(CourseStatus.DRAFT.name());
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
     * Update directly to table if CourseStatus is DRAFT
     * else update to temp table.
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

        if (CourseStatus.valueOf(courseDTO.getStatus()) == CourseStatus.DRAFT){
            // save update to main table
            Course course = courseMapper.toEntity(courseDTO);
            courseRepository.save(course);
        } else {
            // save update to temp table for later review
            contentChangeService.saveEditingCourse(
                    courseDTO,
                    CourseChangeAction.UPDATE,
                    instructorId);
        }
    }

    @Transactional
    public CourseDTO getEditingCourseDTO(Long id) throws Exception {
        Course course = findById(id);
        CourseChangeTemporary changeTemporary = contentChangeService.getCourseEditing(
                EntityType.COURSE,
                id).orElse(null);
        if (changeTemporary == null) {
            return courseMapper.toDTO(course);
        } else {
            String changeContext = changeTemporary.getChanges();
            return objectMapper.readValue(changeContext, CourseDTO.class);
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
}
