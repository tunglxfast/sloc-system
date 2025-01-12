package funix.sloc_system.controller;

import funix.sloc_system.entity.ForgotPassword;
import funix.sloc_system.entity.User;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.security.SecurityUser;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import funix.sloc_system.service.UserService;
import funix.sloc_system.service.EmailService;
import funix.sloc_system.repository.ForgotPasswordRepository;
import funix.sloc_system.service.CourseService;
import java.util.List;

/**
 * Control login, logout.
 */
@Controller
public class AuthController {
    private final String FORGOT_PASSWORD_URL = "forgot_password_form";

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CourseService courseService;

    @GetMapping(value={"", "/", "/login"})
    public String login(@AuthenticationPrincipal SecurityUser securityUser) {
        if (securityUser == null) {
            return "login_form";
        } else {
            return "redirect:/home";
        }
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        if (securityUser == null || securityUser.getUserId() == null) {
            return "redirect:/login";
        }
        List<CourseDTO> topThreeCourses = courseService.getTopThreeCourses();
        model.addAttribute("username", securityUser.getUsername());
        model.addAttribute("topCourses", topThreeCourses);
        return "home";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return FORGOT_PASSWORD_URL;
    }

    @PostMapping("/forgot-password")
    public String sendResetPasswordEmail(@RequestParam String email, Model model) {
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("errorMessage", "Email not found.");
            return FORGOT_PASSWORD_URL;
        }

        try {
            sendResetPasswordEmail(user.getUsername(), email);
            model.addAttribute("successMessage", "Reset password request was sent to your email.");
            return FORGOT_PASSWORD_URL;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to send reset password email.");
            return FORGOT_PASSWORD_URL;
        }
    }


    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam String token, Model model) {
        ForgotPassword forgotPassword = forgotPasswordRepository.findByToken(token).orElse(null);
        if (!checkForgotPasswordToken(forgotPassword)) {
            model.addAttribute("errorMessage", "Invalid token.");
            return "redirect:/login";
        }
        String username = forgotPassword.getUsername();
        model.addAttribute("username", username);
        model.addAttribute("token", token);
        return "reset_password_form";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password, 
                                @RequestParam String confirmPassword,
                                Model model) {
        ForgotPassword forgotPassword = forgotPasswordRepository.findByToken(token).orElse(null);
        if (!checkForgotPasswordToken(forgotPassword)) {
            model.addAttribute("errorMessage", "Invalid token.");
            return "redirect:/login";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Password and confirm password do not match.");
            return "reset_password_form";
        }
        
        User user = userService.findByUsername(forgotPassword.getUsername());
        user.setPassword(passwordEncoder.encode(password));
        userService.updateUser(user);
        if (forgotPassword != null) {
            forgotPasswordRepository.delete(forgotPassword);
            forgotPasswordRepository.flush();
        }
        return "redirect:/login";
    }

    // Helper method to send reset password email
    private void sendResetPasswordEmail(String username, String email) throws Exception {
        ForgotPassword forgotPassword = forgotPasswordRepository.findByEmail(email).orElse(null);
        if (forgotPassword == null) {
            forgotPassword = new ForgotPassword();
        }
        // Generate verification token
        String token = UUID.randomUUID().toString();
        forgotPassword.setUsername(username);
        forgotPassword.setEmail(email);
        forgotPassword.setToken(token);
        forgotPassword.setExpiryDate(LocalDateTime.now().plusHours(1));
        forgotPasswordRepository.save(forgotPassword);  
        
        // Send verification email
        emailService.sendResetPasswordEmail(username, email, token);
    }

    private boolean checkForgotPasswordToken(ForgotPassword forgotPassword) {
        if (forgotPassword == null) {
            return false;
        }
        if (forgotPassword.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }
}
