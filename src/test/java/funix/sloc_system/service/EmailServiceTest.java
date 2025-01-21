package funix.sloc_system.service;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class EmailServiceTest {
    @MockBean
    private JavaMailSender mailSender;
    @Autowired
    private EmailService emailService;
    private final String TEST_MAIL = "funixtesting12972@gmail.com";

    @BeforeEach
    public void setUp() {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        Mockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    public void testSendEmail() {
        String to = TEST_MAIL;
        String subject = "Hello";
        String body = "Good day";
        emailService.sendEmail(to, subject, body);

        SimpleMailMessage expectedMessage = new SimpleMailMessage();
        expectedMessage.setTo(to);
        expectedMessage.setSubject(subject);
        expectedMessage.setText(body);
        assertDoesNotThrow(() -> Mockito.verify(mailSender, Mockito.times(1)).send(expectedMessage));
    }

    @Test
    public void testSendEmailHTML() throws Exception {
        String to = TEST_MAIL;
        String subject = "Hello";
        String body = "Good day";
        emailService.sendEmailHTML(to, subject, body);

        // Capture the argument passed to method
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        Mockito.verify(mailSender, Mockito.times(1)).send(captor.capture());

        MimeMessage capturedArgument = captor.getValue();
        assertEquals(subject, capturedArgument.getSubject());
        assertEquals(body, capturedArgument.getContent());
    }

    @Test
    public void testSendRejectionEmail() throws Exception {
        User instructor = new User();
        instructor.setEmail(TEST_MAIL);
        instructor.setFullName("instructor");
        Course course = new Course();
        course.setTitle("New course");
        String reason = "No reasom";
        emailService.sendRejectionEmail(instructor, course, reason);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        Mockito.verify(mailSender, Mockito.times(1)).send(captor.capture());

        SimpleMailMessage capturedArgument = captor.getValue();
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

        assertEquals(TEST_MAIL, capturedArgument.getTo()[0]);
        assertEquals(subject, capturedArgument.getSubject());
        assertEquals(body, capturedArgument.getText());
    }

    @Test
    public void testSendApprovalEmail() throws Exception {
        User instructor = new User();
        instructor.setEmail(TEST_MAIL);
        instructor.setFullName("instructor");
        Course course = new Course();
        course.setTitle("New course");
        emailService.sendApprovalEmail(instructor, course);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        Mockito.verify(mailSender, Mockito.times(1)).send(captor.capture());

        SimpleMailMessage capturedArgument = captor.getValue();
        String subject = "Course approve: " + course.getTitle();
        String body = String.format(
                "Dear %s,%n%n"
                        + "Your course '%s' has been approved.%n"
                        + "Thank you for your hard work and dedication.%n"
                        + "We look forward to seeing your course in action.%n"
                        + "Best regards,%n"
                        + "The SLOC Team",
                instructor.getFullName(), course.getTitle());

        assertEquals(TEST_MAIL, capturedArgument.getTo()[0]);
        assertEquals(subject, capturedArgument.getSubject());
        assertEquals(body, capturedArgument.getText());
    }

    @Test
    public void testSendVerificationEmail() throws Exception {
        User user = new User();
        user.setEmail(TEST_MAIL);
        user.setFullName("instructor");
        String token = "this is a test Verification token";
        emailService.sendVerificationEmail(user, token);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        Mockito.verify(mailSender, Mockito.times(1)).send(captor.capture());

        MimeMessage capturedArgument = captor.getValue();
        String subject = "Verify your account";
        assertEquals(subject, capturedArgument.getSubject());
        assertTrue(capturedArgument.getContent().toString().contains(token));
    }

    @Test
    public void testSendNewVerificationEmail() throws Exception {
        User user = new User();
        user.setEmail(TEST_MAIL);
        user.setFullName("instructor");
        String token = "this is a test New Verification token";
        emailService.sendNewVerificationEmail(user, token);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        Mockito.verify(mailSender, Mockito.times(1)).send(captor.capture());

        MimeMessage capturedArgument = captor.getValue();
        String subject = "Request new verification email";
        assertEquals(subject, capturedArgument.getSubject());
        assertTrue(capturedArgument.getContent().toString().contains(token));
    }

    @Test
    public void testSendResetPasswordEmail() throws Exception {
        String token = "this is a test Reset Password token";
        emailService.sendResetPasswordEmail("instructor", TEST_MAIL, token);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        Mockito.verify(mailSender, Mockito.times(1)).send(captor.capture());

        MimeMessage capturedArgument = captor.getValue();
        String subject = "Reset your password";
        assertEquals(subject, capturedArgument.getSubject());
        assertTrue(capturedArgument.getContent().toString().contains(token));
    }
}
