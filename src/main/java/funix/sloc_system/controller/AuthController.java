package funix.sloc_system.controller;

import funix.sloc_system.security.SecurityUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Control login, logout.
 */
@Controller
public class AuthController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping(value={"", "/", "/login"})
    public String login(@AuthenticationPrincipal SecurityUser securityUser) {
        if (securityUser == null) {
            return "login-form";
        } else {
            return "home";
        }
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        if (securityUser == null || securityUser.getUserId() == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", securityUser.getUsername());
        return "home";
    }

    // @GetMapping("/logout")
    // public String logout(HttpSession session) {
    //     if (session != null && session.getAttributeNames() != null) {
    //         session.invalidate();
    //     }

    //     // Delete security info
    //     SecurityContextHolder.getContext().setAuthentication(null);
    //     SecurityContextHolder.clearContext();

    //     return "redirect:/login?logout";
    // }
}
