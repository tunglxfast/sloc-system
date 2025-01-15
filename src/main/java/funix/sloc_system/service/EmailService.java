package funix.sloc_system.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendEmailHTML(String to, String subject, String body) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new Exception("Failed to send email");
        }
    }

    
    public void sendRejectionEmail(User instructor, Course course, String reason) {
        String subject = String.format("Course Rejected: %s", course.getTitle());
        String body = String.format(
                "Dear %s,%n%n"
                + "Your course '%s' has been rejected.%n"
                + "Reason: %n %s %n%n"
                + "Please make necessary changes and resubmit again.%n"
                + "Thank you.%n"
                + "Best regards,%n"
                + "The SLOC Team",
                instructor.getFullName(), course.getTitle(), reason
        );
        sendEmail(instructor.getEmail(), subject, body);
    }

    public void sendApprovalEmail(User instructor, Course course) {
        String subject = "Course approve: " + course.getTitle();
        String body = String.format(
                "Dear %s,%n%n"
                + "Your course '%s' has been approved.%n"
                + "Thank you for your hard work and dedication.%n"
                + "We look forward to seeing your course in action.%n"
                + "Best regards,%n"
                + "The SLOC Team",
                instructor.getFullName(), course.getTitle());
        sendEmail(instructor.getEmail(), subject, body);
    }

    public void sendVerificationEmail(User user, String token) throws Exception {
        String subject = "Verify your account";
        String verificationLink = baseUrl + "/verify?token=" + token;
        String body = String.format(
                "Hello %s, <br>"
                + "Thank you for registering. Please click the link below to verify your email: <br>"
                + "<a href=\"%s\">%s</a><br>"
                + "This link will expire in 24 hours.<br>"
                + "If you did not request this, please ignore this email.",
                user.getFullName(), verificationLink, verificationLink
        );
        sendEmailHTML(user.getEmail(), subject, body);
    }

    public void sendNewVerificationEmail(User user, String token) throws Exception {
        String subject = "Request new verification email";
        String verificationLink = baseUrl + "/verify?token=" + token;
        String body = String.format(
                "Hello %s, <br>"
                + "You have requested to resend the verification email. Please click the link below to verify your email: <br>"
                + "<a href=\"%s\">%s</a><br>"
                + "This link will expire in 24 hours.%n"
                + "If you did not request this, please ignore this email.",
                user.getFullName(), verificationLink, verificationLink
        );
        sendEmailHTML(user.getEmail(), subject, body);
    }

    /**
     * Send reset password email.
     * @param username
     * @param email
     * @param token
     */
    public void sendResetPasswordEmail(String username, String email, String token) throws Exception {
        String subject = "Reset your password";
        String resetLink = baseUrl + "/reset-password?token=" + token;
        String body = String.format(
            "Hello %s, <br>" 
            + "You have requested to reset your password. Please click the link below to reset your password: <br>"
            + "<a href=\"%s\">%s</a><br>"
            + "This link will expire in 1 hours.%n"
            + "If you did not request this, please ignore this email.", 
            username, resetLink, resetLink);
        sendEmailHTML(email, subject, body);
    }
}
