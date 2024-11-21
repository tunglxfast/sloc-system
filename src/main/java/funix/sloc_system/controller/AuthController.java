package funix.sloc_system.controller;

import funix.sloc_system.dao.UserDao;
import funix.sloc_system.entity.User;
import funix.sloc_system.security.SecurityUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Điều khiển login, logout.
 */
@Controller
public class AuthController {
    private final UserDao userDAO;

    public AuthController(UserDao userDAO) {
        this.userDAO = userDAO;
    }

    @GetMapping(value={"", "/", "/login"})
    public String login() {
        return "login-form";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        User user = securityUser.getUser();
        if (user == null) {
            return "redirect:/login"; // Chuyển đến trang đăng nhập
        }
        model.addAttribute("user", user);
        return "home";  // Trang chủ của người dùng sau khi đăng nhập thành công
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null && session.getAttributeNames() != null) {
            session.invalidate();
        }

        // Xóa thông tin bảo mật
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();

        return "redirect:/login?logout";
    }
}
