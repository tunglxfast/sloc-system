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

@Controller
@RequestMapping("/instructor/course")
public class CourseManagementController {
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private ServletContext servletContext;

    /**
     * Show create course form
     * @param securityUser
     * @param model
     * @return
     */
    @GetMapping("/create")
    public String showCreateCourseForm(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        User instructor = userService.findById(securityUser.getUser().getId());
        model.addAttribute("user", instructor);
        model.addAttribute("course", new Course());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "instructor/create_course";
    }

    /**
     * Save course if create new or draft.
     * Save to AuditLog if update course
     */
    @PostMapping("/save")
    public String createCourse(@ModelAttribute("course") Course course,
                               @AuthenticationPrincipal SecurityUser securityUser,
                               @RequestParam("thumbnailFile") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        User instructor = securityUser.getUser();
        try {
            String message = courseService.createOrUpdateCourse(course, instructor, file);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "An error occurred while creating the course.");
        }
        redirectAttributes.addFlashAttribute("course", course);
        return String.format("redirect:/instructor/course/edit/%s", course.getId());
    }

    @GetMapping("/edit/{courseId}")
    public String showEditingCourse(@PathVariable("courseId") Long courseId,
                                    @ModelAttribute("course") Course editingCourse,
                                    Model model) {
        Course course;
        if (editingCourse == null) {
            course = courseService.findById(courseId);
        } else {
            course = editingCourse;
        }

        if (course == null) {
            return "redirect:/instructor";
        } else {
            model.addAttribute("course", course);
            return "instructor/edit_course";
        }
    }

    @PostMapping("/edit/{courseId}")
    public String submitReviewCourse(@PathVariable("courseId") Long courseId,
                                     @AuthenticationPrincipal SecurityUser securityUser,
                                     @ModelAttribute("course") Course course,
                                     @RequestParam("thumbnailFile") MultipartFile file,
                                     RedirectAttributes redirectAttributes) {

        User instructor = securityUser.getUser();
        try {
            courseService.createOrUpdateCourse(course, instructor, file);
            courseService.sendToReview(course);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Submit sucessfully! Please waiting for moderator to review course.");
            return "redirect:instructor/courses";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
