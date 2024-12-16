package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dao.*;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.*;
import funix.sloc_system.enums.CourseChangeAction;
import funix.sloc_system.enums.CourseStatus;
import funix.sloc_system.enums.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CourseService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private CourseDao courseDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private ChapterDao chapterDao;
    @Autowired
    private EnrollmentDao enrollmentDao;
    @Autowired
    private EmailService emailService;
    @Autowired
    private CourseChangeTemporaryService courseChangeTemporaryService;
    @Autowired
    private ObjectMapper objectMapper;

    public List<Course> getAllCourses() {
        return courseDao.findAll();
    }

    public Course findById(Long id) {
        return courseDao.findById(id).orElse(null);
    }

    public List<Course> findByCategoryId(Long categoryId) {
        return courseDao.findByCategoryId(categoryId);
    }

    public Course save(Course course) {
        return courseDao.save(course);
    }

    public List<Course> findAllByInstructorAndStatus(User instructor, CourseStatus status) {
        return courseDao.findAllByInstructorAndStatus(instructor, status);
    }

    public List<Course> getApprovedOrUpdatingCourses() {
        return courseDao.findByStatusIn(List.of(CourseStatus.APPROVED, CourseStatus.PENDING_EDIT));
    }

    public void submitForCreatingReview(Long courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getChapters().isEmpty()) {
            throw new IllegalArgumentException("Course must have at least one chapter before submission.");
        }

        course.setStatus(CourseStatus.PENDING_CREATE);
        courseDao.save(course);
    }

    public void submitForReview(Long courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // change course status for reviewing
        if (course.getStatus() == CourseStatus.DRAFT){
            course.setStatus(CourseStatus.PENDING_CREATE);
        } else {
            course.setStatus(CourseStatus.PENDING_EDIT);
        }
        courseDao.save(course);
    }

    public void sendRejectionEmail(Set<User> instructors, Course course, String reason) {
        for (User instructor: instructors){
            sendRejectionEmail(instructor, course, reason);
        }
    }

    public void sendRejectionEmail(User instructor, Course course, String reason) {
        String subject = String.format("Course Rejected: %s", course.getTitle());
        String body = String.format(
                "Dear %s,%n%n"
                + "Your course '%s' has been rejected.%n"
                + "Reason: %s %n%n"
                + "Please review and make necessary changes.%n"
                + "Thank you.",
                instructor.getName(), course.getTitle(), reason
        );
        emailService.sendEmail(instructor.getEmail(), subject, body);
    }

    public void sendApproveEmail(Set<User> instructors, Course course) {
        for (User instructor: instructors){
            sendApproveEmail(instructor, course);
        }
    }

    public void sendApproveEmail(User instructor, Course course) {
        String subject = "Course approve: " + course.getTitle();
        String body = String.format(
                "Dear %s,%n%n"
                + "Your course '%s'"
                + "has been approved.%n"
                + "Thank you.",
                instructor.getName(), course.getTitle());
        emailService.sendEmail(instructor.getEmail(), subject, body);
    }

    @Transactional
    public List<Course> findByInstructor(User instructor) {
        return courseDao.findByInstructors(instructor);
    }

    @Transactional
    public CourseDTO createDraftCourse(CourseDTO courseDTO, User instructor, MultipartFile file) throws IOException {
        Course course = reconvertToEntity(courseDTO);
        course.getInstructors().add(instructor);
        course.setCreatedAt(LocalDate.now());
        course.setCreatedBy(instructor);

        String thumbnailUrl = saveThumbnail(file);
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()){
            course.setThumbnailUrl(thumbnailUrl);
        }

        courseDao.save(course);
        return convertToDTO(course);
    }

    /**
     * Update directly to table if CourseStatus is DRAFT
     * else update to temp table.
     */
    @Transactional
    public void saveUpdateCourse(CourseDTO courseDTO, Long instructorId, MultipartFile file) throws IOException {
        courseDTO.getInstructors().add(instructorId);
        String thumbnailUrl = saveThumbnail(file);
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()){
            courseDTO.setThumbnailUrl(thumbnailUrl);
        }
        courseDTO.setUpdatedAt(LocalDate.now());
        courseDTO.setLastUpdatedBy(instructorId);

        if (CourseStatus.valueOf(courseDTO.getStatus()) == CourseStatus.DRAFT){
            // save update to main table
            Course course = reconvertToEntity(courseDTO);
            courseDao.save(course);
        } else {
            // save update to temp table for later review
            courseChangeTemporaryService.saveEditingCourse(
                    courseDTO,
                    CourseChangeAction.UPDATE,
                    instructorId);
        }
    }

    @Transactional
    public CourseDTO getEditingCourseDTO(Long id) throws Exception {
        Course course = findById(id);
        CourseChangeTemporary changeTemporary = courseChangeTemporaryService.getCourseEditing(
                EntityType.COURSE,
                id).orElse(null);
        if (changeTemporary == null) {
            return convertToDTO(course);
        } else {
            String changeContext = changeTemporary.getChanges();
            return objectMapper.readValue(changeContext, CourseDTO.class);
        }
    }

    @Transactional
    private String saveThumbnail(MultipartFile file) throws IOException {
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

    /**
     * Courvert Course to CourseDTO
     * @param course
     * @return CourseDTO
     */
    public CourseDTO convertToDTO(Course course) {
        if (course == null){
            return null;
        }

        Set<Long> chapters = new HashSet<>();
        Set<Long> enrollments = new HashSet<>();
        Set<Long> instructors = new HashSet<>();

        for (Chapter chapter : course.getChapters()){
            chapters.add(chapter.getId());
        }

        for (Enrollment enrollment: course.getEnrollments()){
            enrollments.add(enrollment.getId());
        }

        for (User instructor : course.getInstructors()){
            instructors.add(instructor.getId());
        }

        return new CourseDTO(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getCategory() != null ? course.getCategory().getId() : null,
                course.getCreatedBy() != null ? course.getCreatedBy().getId() : null,
                course.getLastUpdatedBy() != null ? course.getLastUpdatedBy().getId() : null,
                course.getStartDate(),
                course.getEndDate(),
                course.getStatus().name(),
                course.getRejectReason(),
                course.getCreatedAt(),
                course.getUpdatedAt(),
                chapters,
                enrollments,
                instructors
        );
    }

    /**
     * Courvert CourseDTO to Course entity
     * @param courseDTO
     * @return Course
     */
    public Course reconvertToEntity(CourseDTO courseDTO) {
        if (courseDTO == null){
            return null;
        }

        Set<Chapter> chapters = new HashSet<>();
        Set<Enrollment> enrollments = new HashSet<>();
        Set<User> instructors = new HashSet<>();
        for (Long chapterId : courseDTO.getChapters()){
            Chapter chapter = chapterDao.findById(chapterId).orElse(null);
            if (chapter != null) {
                chapters.add(chapter);
            }
        }

        for (Long enrollmentId: courseDTO.getEnrollments()){
            Enrollment enrollment = enrollmentDao.findById(enrollmentId).orElse(null);
            if (enrollment != null) {
                enrollments.add(enrollment);
            }
        }

        for (Long instructorId : courseDTO.getInstructors()){
            User instructor = userDao.findById(instructorId).orElse(null);
            if (instructor != null) {
                instructors.add(instructor);
            }
        }

        Course course = new Course();
        course.setId(courseDTO.getId());
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setThumbnailUrl(courseDTO.getThumbnailUrl());

        course.setCategory(courseDTO.getCategory() != null ?
                categoryDao.findById(courseDTO.getCategory()).orElse(null) : null);

        course.setCreatedBy(courseDTO.getCreatedBy() != null ?
                userDao.findById(courseDTO.getCreatedBy()).orElse(null) : null);

        course.setLastUpdatedBy(courseDTO.getLastUpdatedBy() != null ?
                userDao.findById(courseDTO.getLastUpdatedBy()).orElse(null) : null);

        course.setStartDate(courseDTO.getStartDate());
        course.setEndDate(courseDTO.getEndDate());

        course.setStatus(courseDTO.getStatus() != null ?
                CourseStatus.valueOf(courseDTO.getStatus()) : CourseStatus.DRAFT);

        course.setRejectReason(courseDTO.getRejectReason());
        course.setCreatedAt(courseDTO.getCreatedAt());
        course.setUpdatedAt(courseDTO.getUpdatedAt());
        course.setChapters(chapters);
        course.setEnrollments(enrollments);
        course.setInstructors(instructors);

        return course;
    }

    public boolean courseExists(Long courseId) {
        return courseDao.existsById(courseId);
    }

    public boolean isEditable(Long courseId){
        Course course = findById(courseId);
        if (course == null) {
            return false;
        }

        CourseStatus status = course.getStatus();
        if (status.equals(CourseStatus.PENDING_EDIT) || status.equals(CourseStatus.PENDING_CREATE)) {
            return false;
        }

        return true;
    }
}
