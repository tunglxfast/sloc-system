package funix.sloc_system.controller;


import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.CategoryService;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.UserService;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Paths;

@Controller
@RequestMapping("/instructor/course")
public class CreatingCourseController {
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private ServletContext servletContext;

    @GetMapping("/create")
    public String showCreateCourseForm(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        User instructor = userService.findById(securityUser.getUser().getId());
        model.addAttribute("user", instructor);
        model.addAttribute("course", new Course());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "instructor/create_course";
    }

    @GetMapping("/{courseId}/edit")
    public String showCreatingCourse(@PathVariable("courseId") Long courseId, Model model) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            return "redirect:/instructor";
        }
        model.addAttribute("course", course);
        return "instructor/edit_course";
    }

    @PostMapping("/{courseId}/review_submit")
    public String reviewCourseSubmit(@PathVariable("courseId") Long courseId, RedirectAttributes redirectAttributes) {
        courseService.submitForReview(courseId);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Waiting for moderator to review course.");
        return "redirect:/instructor";
    }

    @PostMapping("/create_submit")
    public String createCourse(@ModelAttribute("course") Course course,
                               @AuthenticationPrincipal SecurityUser securityUser,
                               @RequestParam("thumbnailFile") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        try {
            course = courseService.createCourse(course, securityUser.getUser().getId());
            // save picture
            if (!file.isEmpty()) {
                String fileName = "thumbnail_" + course.getId() + ".jpg";
                String absolutePath = Paths.get("").toAbsolutePath().toString() + "/src/main/resources/static/img/";
                File saveFile = new File(absolutePath + fileName);
                file.transferTo(saveFile);

                course.setThumbnailUrl("/img/" + fileName);
            }
            course = courseService.save(course);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "The course general has been created.");
            return String.format("redirect:/instructor/course/%s/chapters", course.getId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "An error occurred while creating the course.");
            return "redirect:/instructor";
        }
    }
}