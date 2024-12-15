package funix.sloc_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dao.CourseDao;
import funix.sloc_system.dao.UserDao;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.EditCourseAudit;
import funix.sloc_system.entity.User;
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
import java.util.List;
import java.util.Set;

@Service
public class CourseService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private CourseDao courseDao;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EditCourseAuditService editCourseAuditService;
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
        return courseDao.findByStatusIn(List.of(CourseStatus.APPROVED, CourseStatus.UPDATING));
    }

    public void submitForReview(Long courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getChapters().isEmpty()) {
            throw new IllegalArgumentException("Course must have at least one chapter before submission.");
        }

        course.setStatus(CourseStatus.PENDING);
        courseDao.save(course);
    }

    public void submitForUpdateReview(Long courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        course.setStatus(CourseStatus.UPDATING);
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


    // Save directly if still in DRAFT
    @Transactional
    public String createOrUpdateCourse(Course course, User instructor, MultipartFile file) throws IOException {
        course.getInstructors().add(instructor);
        saveCourseThumbnail(course, file);
        String returnMessage;
        if (course.getId() == null) {
            // create new Course
            course.setCreatedAt(LocalDate.now());
            course.setCreatedBy(instructor.getId());
            returnMessage = "The course general has been created.";
        } else if (course.getStatus() == CourseStatus.DRAFT){
            // save update draft to main table
            course.setUpdatedAt(LocalDate.now());
            course.setLastUpdatedBy(instructor.getId());
            returnMessage = "The course general has been updated.";
        } else {
            // save update course not draft to audit table for later review
            editCourseAuditService.saveEditingCourse(
                    EntityType.COURSE.name(),
                    course,
                    CourseStatus.UPDATING.name(),
                    instructor);
            returnMessage = "The course general has been updated.";
        }
        courseDao.save(course);
        return returnMessage;
    }

    @Transactional
    public void sendToReview(Course course) throws Exception {

        if (course.getStatus() == CourseStatus.DRAFT) {
            submitForReview(course.getId());
        } else {
            submitForUpdateReview(course.getId());
        }
    }

    private Course getEditingCourse(Long id) throws JsonProcessingException {
        EditCourseAudit courseAudit = editCourseAuditService.getLatestAudits(
                EntityType.COURSE.name(),
                id,
                CourseStatus.PENDING.name()
                ).orElse(null);
        if (courseAudit == null) {
            return courseDao.findById(id).orElse(null);
        } else {
            String courseContext = courseAudit.getChanges();
            return objectMapper.readValue(courseContext, Course.class);
        }
    }

    private void saveCourseThumbnail(Course course, MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String fileName = "thumbnail_" + course.getId() + ".jpg";
            String absolutePath = Paths.get("").toAbsolutePath().toString() + "/src/main/resources/static/img/";
            File saveFile = new File(absolutePath + fileName);
            file.transferTo(saveFile);
            course.setThumbnailUrl("/img/" + fileName);
        }
    }
}
