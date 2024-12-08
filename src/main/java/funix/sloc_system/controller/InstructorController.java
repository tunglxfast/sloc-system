package funix.sloc_system.controller;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.CourseStatus;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.CategoryService;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
    private CategoryService categoryService;

    @GetMapping(value = {"","/","instructor_courses"})
    public String showInstructorManageList(@AuthenticationPrincipal SecurityUser securityUser, Model model){
        User instructor = userService.findById(securityUser.getUser().getId());
        List<Course> courseList = courseService.findByIntructors(instructor);
        model.addAttribute("user", instructor);
        model.addAttribute("courses", courseList);
        return "instructor/instructor_courses";
    }

    @GetMapping("/course/create")
    public String showCreateCourseForm(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        User instructor = userService.findById(securityUser.getUser().getId());
        model.addAttribute("user", instructor);
        model.addAttribute("course", new Course());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "instructor/create_course";
    }

    @PostMapping("/course/create_submit")
    public String createCourse(@ModelAttribute("course") Course course,
                               @AuthenticationPrincipal SecurityUser securityUser,
                               @RequestParam("thumbnailFile") MultipartFile file,
                               Model model) {
        User instructor = userService.findById(securityUser.getUser().getId());
        try {
            course = courseService.createCourse(course, securityUser.getUser().getId());
            // save picture
            if (!file.isEmpty()) {
                String fileName = "thumbnail_" + course.getId() + ".jpg";
                File saveFile = new File("src/main/resources/static/img/" + fileName);
                file.transferTo(saveFile);

                course.setThumbnailUrl("/img/" + fileName);
            }
            course = courseService.save(course);

            model.addAttribute(
                    "successMessage",
                    "The course general has been created.");
            model.addAttribute("user", instructor);
            model.addAttribute("course", course);
            return "instructor/create_topic";
        } catch (Exception e) {
            model.addAttribute(
                    "errorMessage",
                    "An error occurred while creating the course.");
            model.addAttribute("user", instructor);
            model.addAttribute("course", new Course());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "instructor/create_course";
        }
    }

    @GetMapping("/instructor/notifications")
    public String getNotifications(Principal principal, Model model) {
        User instructor = userService.findByUsername(principal.getName());
        List<Course> rejectedCourses = courseService.findAllByInstructorAndStatus(instructor, CourseStatus.REJECTED);
        model.addAttribute("rejectedCourses", rejectedCourses);
        return "instructor/notifications";
    }
}
