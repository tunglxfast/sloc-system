package funix.sloc_system.service;

import funix.sloc_system.dao.CourseDao;
import funix.sloc_system.dao.UserDao;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.CourseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<Course> findByInstructors(User instructor) {
        return courseDao.findByInstructors(instructor);
    }

    @Transactional
    public void submitForApproval(Long courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        course.setStatus(CourseStatus.PENDING);
        courseDao.save(course);
    }
}
