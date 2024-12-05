package funix.sloc_system.service;

import funix.sloc_system.dao.CourseDao;
import funix.sloc_system.dao.UserDao;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.CourseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class CourseService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private CourseDao courseDAO;
    @Autowired
    private EmailService emailService;

    public List<Course> getAllCourses() {
        return courseDAO.findAll();
    }

    public Course findById(Long id) {
        return courseDAO.findById(id).orElse(null);
    }

    public List<Course> findByCategoryId(Long categoryId) {
        return courseDAO.findByCategoryId(categoryId);
    }

    public Course save(Course course) {
        return courseDAO.save(course);
    }

    public List<Course> findAllByInstructorAndStatus(User instructor, CourseStatus status) {
        return courseDAO.findAllByInstructorAndStatus(instructor, status);
    }

    public Course createCourse(Course course, String username) {
        User creator = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not exist"));

        course.getInstructors().add(creator);
        course.setStatus(CourseStatus.PENDING);
        course.setCreatedAt(LocalDate.now());

        return courseDAO.save(course);
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


}
