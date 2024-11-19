package funix.sloc_system.controller;

import funix.sloc_system.entity.User;
import funix.sloc_system.dao.UserDAO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Điều khiển login, logout.
 */
@Controller
public class AuthController {
    private final UserDAO userDAO;

    public AuthController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("user", user);
            return "redirect:/home";  // Chuyển đến trang chính sau khi đăng nhập
        }
        model.addAttribute("error", "Invalid credentials");
        return "login";
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // Chuyển đến trang đăng nhập
        }
        model.addAttribute("user", user);
        return "home";  // Trang chủ của người dùng sau khi đăng nhập thành công
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // Đăng xuất người dùng
        return "redirect:/login";
    }
}
