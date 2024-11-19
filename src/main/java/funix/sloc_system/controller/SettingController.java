package funix.sloc_system.controller;

import funix.sloc_system.entity.Enrollment;
import funix.sloc_system.entity.User;
import funix.sloc_system.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/setting")
public class SettingController {

    @Autowired
    private EnrollmentService enrollmentService;

    // Xem các khóa học người dùng đã tham gia
    @GetMapping("/my-courses")
    public String myCourses(@AuthenticationPrincipal User user, Model model) {
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByUser(user);
        model.addAttribute("enrollments", enrollments);
        return "course/my-courses";
    }
}
