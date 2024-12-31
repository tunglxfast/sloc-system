package funix.sloc_system.controller;

import funix.sloc_system.entity.Enrollment;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

@Controller
@RequestMapping("/setting")
public class SettingController {

    @Autowired
    private EnrollmentService enrollmentService;

    // Show user enrolled courses
    @GetMapping("/my-courses")
    public String myCourses(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        Long userId = securityUser.getUserId();
        Set<Enrollment> enrollments = enrollmentService.getEnrollmentsByUserId(userId);
        model.addAttribute("enrollments", enrollments);
        return "course/my-courses";
    }
}
