package funix.sloc_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.EmailService;

@Controller
public class SupportController {

    @Value("${support.email}")
    private String supportEmail;

    @Autowired
    private EmailService emailService;

    @GetMapping("/support")
    public String showSupportForm(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
      if (securityUser != null) {
        model.addAttribute("showNavbar", true);
      }
      return "support";
    }

    @PostMapping("/support")
    public String handleSupportRequest(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam("email") String email,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            RedirectAttributes redirectAttributes) {
        // Send support email
        try {
            sendSupportEmail(email, subject, message);
            if (securityUser != null) {
              redirectAttributes.addFlashAttribute("showNavbar", true);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Your message has been sent successfully.");
            return "redirect:/support";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send your message. Please try again later.");
            return "redirect:/support";
        }
    }

    private void sendSupportEmail(String email, String subject, String message) {
      String supportSubject = "SLOC System Support Request: from" + email;
      String supportMessage = String.format("From: %s %n%n Support Request:%n%s", email, message);

      emailService.sendEmail(supportEmail, supportSubject, supportMessage);
    }
}
