package funix.sloc_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dao.CourseDao;
import funix.sloc_system.dao.UserDao;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.CourseChangeTemporary;
import funix.sloc_system.entity.User;
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

    public void submitForReview(Course course) {
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
            courseDao.save(course);
            returnMessage = "The course general has been created.";
        } else if (course.getStatus() == CourseStatus.DRAFT){
            // save update draft to main table
            course.setUpdatedAt(LocalDate.now());
            course.setLastUpdatedBy(instructor.getId());
            courseDao.save(course);
            returnMessage = "The course general has been updated.";
        } else {
            // save update course not draft to temp table for later review
            courseChangeTemporaryService.saveEditingCourse(
                    course,
                    CourseChangeAction.UPDATE,
                    instructor);
            returnMessage = "The course general has been updated.";
        }
        return returnMessage;
    }

    @Transactional
    public Course getEditingCourse(Long id) throws JsonProcessingException {
        CourseChangeTemporary changeTemporary = courseChangeTemporaryService.getCourseEditing(
                EntityType.COURSE,
                id).orElse(null);
        if (changeTemporary == null) {
            return courseDao.findById(id).orElse(null);
        } else {
            String courseContext = changeTemporary.getChanges();
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
