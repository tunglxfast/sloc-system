package funix.sloc_system.controller;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.RoleType;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.mapper.UserMapper;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.UserService;
import funix.sloc_system.util.AppUtil;
import funix.sloc_system.util.CourseEditingHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/instructor")
public class InstructorController {
    @Autowired
    private UserService userService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private AppUtil appUtil;

    @GetMapping(value = {"", "/", "/courses"})
    public String showInstructorManageList(@AuthenticationPrincipal SecurityUser securityUser, 
                                        Model model, 
                                        RedirectAttributes redirectAttributes){
        User user = userService.findById(securityUser.getUserId());
        UserDTO userDTO = userMapper.toDTO(user);
        List<Course> courseList;
        if (user.getStringRoles().contains(RoleType.MODERATOR.name())) {
            courseList = courseService.getAllCourses();
        } else {
            courseList = courseService.findByInstructor(user);
        }

        List<CourseDTO> courseDTOList = courseMapper.toDTO(courseList);
        List<CourseEditingHolder> courseEditingHolders = appUtil.getCourseEditingHolders(courseDTOList);
        model.addAttribute("user", userDTO);
        model.addAttribute("courseEditingHolders", courseEditingHolders);
        model.addAttribute("courses", courseDTOList);
        return "instructor/instructor_courses";
    }

    @GetMapping("/notifications")
    public String getNotifications(Principal principal, Model model) {
        User instructor = userService.findByUsername(principal.getName());
        List<Course> rejectedCourses = courseService.getInstructorRejectedCourses(instructor);
        model.addAttribute("rejectedCourses", rejectedCourses);
        return "instructor/notifications";
    }
}
