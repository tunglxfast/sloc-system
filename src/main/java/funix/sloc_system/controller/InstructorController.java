package funix.sloc_system.controller;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.CourseStatus;
import funix.sloc_system.service.CategoryService;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/course/create")
    public String showCreateCourseForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "instructor/create_course";
    }

    @PostMapping("/course/create_submit")
    public String createCourse(@ModelAttribute("course") Course course,
                               Principal principal,
                               @RequestParam("thumbnailFile") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        try {
            course = courseService.createCourse(course, principal.getName());
            // save picture
            if (!file.isEmpty()) {
                String fileName = "thumbnail_" + course.getId() + ".jpg";
                File saveFile = new File("src/main/resources/static/img/" + fileName);
                file.transferTo(saveFile);

                course.setThumbnailUrl("/img/" + fileName);
            }
            courseService.save(course);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "The course has been created and is awaiting approval.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "An error occurred while creating the course.");
        }
        return "redirect:/instructor/courses";
    }

    @GetMapping("/instructor/notifications")
    public String getNotifications(Principal principal, Model model) {
        User instructor = userService.findByUsername(principal.getName());
        List<Course> rejectedCourses = courseService.findAllByInstructorAndStatus(instructor, CourseStatus.REJECTED);
        model.addAttribute("rejectedCourses", rejectedCourses);
        return "instructor/notifications";
    }
}
