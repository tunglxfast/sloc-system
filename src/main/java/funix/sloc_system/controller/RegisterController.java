package funix.sloc_system.controller;

import funix.sloc_system.entity.Role;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.RoleType;
import funix.sloc_system.repository.RoleRepository;
import funix.sloc_system.repository.UserRepository;
import funix.sloc_system.service.EmailService;
import funix.sloc_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Điều khiển trang đăng ký tài khoản.
 */
@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private EmailService emailService;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String register(Model model,
                           @RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String fullName,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes) {
        // Check username or email already exist
        String checkRegistered = userService.checkRegistered(username, email);

        if (checkRegistered.equalsIgnoreCase("Pass")) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setFullName(fullName);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setLocked(false);
            newUser.setFailedAttempts(0);
            registerNewUser(newUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Create user successfully. Please go to email for verification.");
            return "redirect:/login";
        } else {
            model.addAttribute("error-message", checkRegistered);
            return "register";
        }
    }


    @GetMapping("/verify")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByVerificationToken(token)
                .orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid verification token.");
            return "redirect:/login";
        }

        if (user.isVerified()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email already verified.");
            return "redirect:/login";
        }

        if (user.isTokenExpired()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Verification token has expired. Please request a new one.");
            return "redirect:/login";
        }

        // Verify the user and assign STUDENT role
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiryDate(null);

        // Add STUDENT role
        Role studentRole = roleRepository.findByName(RoleType.STUDENT.name()).orElse(null);
        if (studentRole != null) {
            user.setRoles(Set.of(studentRole));
        } else {
          redirectAttributes.addFlashAttribute("errorMessage", "Server error. Please try again later.");
          return "redirect:/login";
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Email verified successfully. You can now log in.");
        return "redirect:/login";
    }

    private User registerNewUser(User user) {
        // Generate verification token
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiryDate(LocalDateTime.now().plusDays(1));
        user.setVerified(false);
        user.setRoles(new HashSet<>());
        User savedUser = userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(savedUser, token);
        return savedUser;
    }
}
