package funix.sloc_system.controller;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.CourseStatus;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/instructor")
public class InstructorController {
    @Autowired
    private UserService userService;
    @Autowired
    private CourseService courseService;

    @GetMapping(value = {"","/","/courses"})
    public String showInstructorManageList(@AuthenticationPrincipal SecurityUser securityUser, Model model){
        User instructor = userService.findById(securityUser.getUser().getId());
        List<Course> courseList = courseService.findByInstructors(instructor);
        model.addAttribute("user", instructor);
        model.addAttribute("courses", courseList);
        return "instructor/instructor_courses";
    }

    @GetMapping("/notifications")
    public String getNotifications(Principal principal, Model model) {
        User instructor = userService.findByUsername(principal.getName());
        List<Course> rejectedCourses = courseService.findAllByInstructorAndStatus(instructor, CourseStatus.REJECTED);
        model.addAttribute("rejectedCourses", rejectedCourses);
        return "instructor/notifications";
    }
}
