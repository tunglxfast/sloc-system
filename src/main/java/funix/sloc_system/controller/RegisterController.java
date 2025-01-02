package funix.sloc_system.controller;

import funix.sloc_system.entity.User;
import funix.sloc_system.repository.UserRepository;
import funix.sloc_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        // Check username or email already exist
        String checkRegistered = userService.checkRegistered(user);

        if (checkRegistered.equalsIgnoreCase("Pass")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setLocked(false);
            user.setFailedAttempts(0);
            userRepository.save(user);
            return "redirect:/login";
        } else {
            model.addAttribute("error-message", checkRegistered);
            return "register";
        }
    }
}
