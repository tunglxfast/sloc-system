package funix.sloc_system.controller;

import funix.sloc_system.dto.CategoryDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.mapper.CategoryMapper;
import funix.sloc_system.mapper.UserMapper;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

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
    private CategoryMapper categoryMapper;
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/create")
    public String showCreateCourseForm(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        UserDTO userDTO = userMapper.toDTO(userService.findById(securityUser.getUserId()));
        List<CategoryDTO> categoryDTOS = categoryMapper.toDTO(categoryService.getAllCategories());
        model.addAttribute("user", userDTO);
        model.addAttribute("course", new CourseDTO());
        model.addAttribute("categories", categoryDTOS);
        return "instructor/create_course";
    }

    @PostMapping("/create")
    public String createNewCourse(@AuthenticationPrincipal SecurityUser securityUser,
                                @ModelAttribute("course") CourseDTO courseDTO,
                                @RequestParam("thumbnailFile") MultipartFile file,
                                @RequestParam("categoryId") Long categoryId,
                                RedirectAttributes redirectAttributes) {
        Long instructorId = securityUser.getUserId();
        try {
            CourseDTO newCourseDTO = courseService.createNewCourse(courseDTO, instructorId, file, categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "The course has been created successfully.");
            return "redirect:/instructor/course/" + newCourseDTO.getId() + "/chapter";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while creating the course.");
            return "redirect:/instructor";
        }
    }

    @GetMapping("/{courseId}/edit")
    public String showEditingCourse(@PathVariable("courseId") Long courseId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (!courseService.courseExists(courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course does not exist.");
            return "redirect:/instructor/courses";
        }

        try {
            CourseDTO courseDTO = courseService.getEditingCourseDTO(courseId);
            if (courseDTO == null) {
                return "redirect:/instructor";
            }
            
            List<CategoryDTO> categoryDTOS = categoryMapper.toDTO(categoryService.getAllCategories());
            model.addAttribute("course", courseDTO);
            model.addAttribute("categories", categoryDTOS);
            return "instructor/edit_course";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred, please try again later");
            return "redirect:/instructor/courses";
        }
    }

    @PostMapping("/{courseId}/submit")
    public String submitCourse(@PathVariable("courseId") Long courseId,
                            @ModelAttribute("course") CourseDTO courseUpdateValues,
                            @AuthenticationPrincipal SecurityUser securityUser,
                            @RequestParam("thumbnailFile") MultipartFile file,
                            @RequestParam("categoryId") Long categoryId,
                            @RequestParam("action") String action,
                            RedirectAttributes redirectAttributes) {
        Long instructorId = securityUser.getUserId();
        try {
            // Get current course state
            CourseDTO courseDTO = courseService.getEditingCourseDTO(courseId);
            courseDTO.updateCourseDTO(courseUpdateValues);
            
            // Save updates
            courseService.saveUpdateCourse(courseId, courseDTO, instructorId, file, categoryId);
            
            // Handle different actions
            if ("Submit".equals(action)) {
                courseService.submitForReview(courseId);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Course submitted successfully! Please wait for moderator review.");
                return "redirect:/instructor/courses";
            } else { // Save Draft
                redirectAttributes.addFlashAttribute("successMessage", "Course draft saved successfully.");
                return "redirect:/instructor/course/" + courseId + "/edit";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            return "redirect:/instructor/course/" + courseId + "/edit";
        }
    }
}
