package funix.sloc_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
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
        sendEmail(instructor.getEmail(), subject, body);
    }

    public void sendApproveEmail(User instructor, Course course) {
        String subject = "Course approve: " + course.getTitle();
        String body = String.format(
                "Dear %s,%n%n"
                + "Your course '%s'"
                + "has been approved.%n"
                + "Thank you.",
                instructor.getFullName(), course.getTitle());
        sendEmail(instructor.getEmail(), subject, body);
    }
}
