package funix.sloc_system.controller;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.mapper.UserMapper;
import funix.sloc_system.repository.UserRepository;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.EnrollmentService;
import funix.sloc_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserSettingController {
    @Autowired
    private UserService userService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping(value = {"", "/", "/settings"})
    public String viewSettings(Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            UserDTO userDTO = userMapper.toDTO(currentUser);
            model.addAttribute("user", userDTO);
            return "user/settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e);
            return "redirect:/login?logout";
        }
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam("fullName") String fullName,
                                @RequestParam("email") String email,
                                Model model) {
        User currentUser = userService.getCurrentUser();

        // Update email
        if (!currentUser.getEmail().equals(email)) {
            if (userRepository.existsByEmail(email)) {
                model.addAttribute("errorMessage", "Email already exists");
                return "user/settings";
            } else {
                currentUser.setEmail(email);
            }
        }

        // Update full name
        currentUser.setFullName(fullName);

        // Save changes
        userService.updateUser(currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("successMessage", "User settings updated successfully.");
        return "user/settings";
    }

    @PostMapping("/settings/change_password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
            } else if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "New password and confirmation do not match.");
            } else if (passwordEncoder.matches(newPassword, currentUser.getPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", "New password is the same as old password.");
            } else {
                currentUser.setPassword(passwordEncoder.encode(newPassword));
                userService.updateUser(currentUser);
                redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
            }

            return "redirect:/user/settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/login?logout";
        }


    }

    // Show user enrolled courses
    @GetMapping("/learning")
    public String showLearningCourse(@AuthenticationPrincipal SecurityUser securityUser,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model) {
        Long userId = securityUser.getUserId();
        Page<Course> coursePage = enrollmentService.getCoursesByUserIdWithPagination(userId, page, size);
        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        return "user/learning_courses";
    }
}
