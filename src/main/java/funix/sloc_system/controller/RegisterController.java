package funix.sloc_system.controller;

import funix.sloc_system.entity.User;
import funix.sloc_system.repository.UserRepository;
import funix.sloc_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Điều khiển trang đăng ký tài khoản.
 */
@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password, @RequestParam String email, @RequestParam String name, Model model) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setName(name);

        // Kiểm tra username hoặc email đã tồn tại
        String checkRegistered = userService.checkRegistered(user);

        if (checkRegistered.equalsIgnoreCase("Pass")) {
            userRepository.save(user);
            return "redirect:/login";
        } else {
            model.addAttribute("error-message", checkRegistered);
            return "register";
        }
    }
}
