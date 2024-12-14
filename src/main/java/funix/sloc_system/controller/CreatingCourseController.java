package funix.sloc_system.controller;


import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.CourseStatus;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.AuditLogService;
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
import java.time.LocalDate;

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
    private AuditLogService auditLogService;
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

    @PostMapping("/save")
    public String createCourse(@ModelAttribute("course") Course course,
                               @AuthenticationPrincipal SecurityUser securityUser,
                               @RequestParam("thumbnailFile") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        User instructor = securityUser.getUser();

        course.getInstructors().add(instructor);
        course.setCreatedAt(LocalDate.now());
        course.setCreatedBy(instructor.getId());
        try {
            // save picture
            if (!file.isEmpty()) {
                String fileName = "thumbnail_" + course.getId() + ".jpg";
                String absolutePath = Paths.get("").toAbsolutePath().toString() + "/src/main/resources/static/img/";
                File saveFile = new File(absolutePath + fileName);
                file.transferTo(saveFile);
                course.setThumbnailUrl("/img/" + fileName);
                }
        } catch (Exception e) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "An error occurred while creating the course.");
                return "redirect:/instructor";
        }

        // Save directly if still in DRAFT
        if (course.getStatus() == null || course.getStatus().equals("DRAFT")) {
            try {
                courseService.save(course);
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
        } else {
            try {
                // Save changes to pending log if approved
                auditLogService.saveEditingCourse(
                        EntityType.COURSE.toString(),
                        course,
                        CourseStatus.UPDATING.toString(),
                        instructor);

                Course tempCourse = courseService.getEditingCourse(course.getId());

                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "The course general has been edited.");
                redirectAttributes.addFlashAttribute("course", tempCourse);
                return String.format("redirect:/instructor/courses/edit/%s", course.getId());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "An error occurred while editing the course.");
                redirectAttributes.addFlashAttribute("course", course);
                return String.format("redirect:/instructor/courses/edit/%s", course.getId());
            }
        }
    }

    @GetMapping("/edit/{courseId}")
    public String showCreatingCourse(@PathVariable("courseId") Long courseId,
                                     @ModelAttribute("course") Course inputCourse,
                                     Model model) {
        Course course;
        if (inputCourse == null) {
            course = courseService.findById(courseId);
        } else {
            course = inputCourse;
        }

        if (course == null) {
            return "redirect:/instructor";
        } else {
            model.addAttribute("course", course);
            return "instructor/edit_course";
        }
    }

    @PostMapping("/send_review/{courseId}")
    public String reviewCourseSubmit(@PathVariable("courseId") Long courseId, RedirectAttributes redirectAttributes) {
        courseService.submitForReview(courseId);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Waiting for moderator to review course.");
        return "redirect:/instructor";
    }
}
