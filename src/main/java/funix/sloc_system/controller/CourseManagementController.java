package funix.sloc_system.controller;

import funix.sloc_system.dto.CategoryDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.entity.ScoreWeight;
import funix.sloc_system.mapper.CategoryMapper;
import funix.sloc_system.mapper.UserMapper;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.CategoryService;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.ScoreWeightService;
import funix.sloc_system.service.UserService;
import funix.sloc_system.util.AppUtil;
import funix.sloc_system.util.RedirectUrlHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private ScoreWeightService scoreWeightService;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AppUtil appUtil;

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
            redirectAttributes.addFlashAttribute("successMessage",
                    "The course has been created successfully.");
            return String.format("redirect:/instructor/course/%d/edit/chapters", newCourseDTO.getId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor";
        }
    }

    @GetMapping("/{courseId}/edit")
    public String showEditingCourse(@PathVariable("courseId") Long courseId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (!courseService.checkCourseExists(courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course does not exist.");
            return RedirectUrlHelper.REDIRECT_INSTRUCTOR_DASHBOARD;
        }

        try {
            CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);
            if (courseDTO == null) {
                return "redirect:/instructor";
            }
            
            List<CategoryDTO> categoryDTOS = categoryMapper.toDTO(categoryService.getAllCategories());
            model.addAttribute("course", courseDTO);
            model.addAttribute("categories", categoryDTOS);
            return "instructor/edit_course";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred, please try again later");
            return RedirectUrlHelper.REDIRECT_INSTRUCTOR_DASHBOARD;
        }
    }

    @PostMapping("/{courseId}/submit")
    public String submitCourse(@PathVariable("courseId") Long courseId,
                            @ModelAttribute("course") CourseDTO editingValues,
                            @AuthenticationPrincipal SecurityUser securityUser,
                            @RequestParam("thumbnailFile") MultipartFile file,
                            @RequestParam("categoryId") Long categoryId,
                            @RequestParam("action") String action,
                            RedirectAttributes redirectAttributes) {
        Long instructorId = securityUser.getUserId();
        try {
            // Save update course based on content status
            courseService.updateCourse(courseId, editingValues, instructorId, file, categoryId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred while updating, please contact support!");
            return String.format("redirect:/instructor/course/%d/edit", courseId);
        }

        try {
            // Handle different actions
            if ("Submit".equals(action)) {
                courseService.submitForReview(courseId);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Course submitted successfully! Please wait for moderator review.");
                return RedirectUrlHelper.REDIRECT_INSTRUCTOR_DASHBOARD;
            } else {
                // Save Draft
                redirectAttributes.addFlashAttribute("successMessage",
                        "Course draft saved successfully.");
                return "redirect:/instructor/course/" + courseId + "/edit";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor/course/" + courseId + "/edit";
        }
    }
    
    @GetMapping("/{courseId}/delete")
    public String deleteCourse(@PathVariable("courseId") Long courseId,
                            @AuthenticationPrincipal SecurityUser securityUser,
                            RedirectAttributes redirectAttributes) {
        Long instructorId = securityUser.getUserId();
        try {
            String successMessage = courseService.deleteCourse(courseId, instructorId);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/instructor/courses";
    }

    @GetMapping("/{courseId}/reset")
    public String resetCourse(@PathVariable("courseId") Long courseId,
                            @AuthenticationPrincipal SecurityUser securityUser,
                            RedirectAttributes redirectAttributes) {
        Long instructorId = securityUser.getUserId();
        try {
            courseService.resetCourse(courseId, instructorId);
            redirectAttributes.addFlashAttribute("successMessage", "Course reset successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/instructor/courses";
    }

    @GetMapping("/{courseId}/score_weight")
    public String showScoreWeightForm(@PathVariable("courseId") Long courseId,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (!courseService.checkCourseExists(courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course does not exist.");
            return RedirectUrlHelper.REDIRECT_INSTRUCTOR_DASHBOARD;
        }

        try {
            ScoreWeight scoreWeight = scoreWeightService.getScoreWeightByCourseId(courseId);
            double quizWeight = ScoreWeightService.DEFAULT_QUIZ_WEIGHT;
            double examWeight = ScoreWeightService.DEFAULT_EXAM_WEIGHT;
            if (scoreWeight != null) {
                quizWeight = scoreWeight.getQuizWeight();
                examWeight = scoreWeight.getExamWeight();
            }
            model.addAttribute("courseId", courseId);
            model.addAttribute("quizWeight", quizWeight);
            model.addAttribute("examWeight", examWeight);
            return "instructor/score_weight";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred, please try again later");
            return RedirectUrlHelper.REDIRECT_INSTRUCTOR_DASHBOARD;
        }
    }

    @PostMapping("/{courseId}/score_weight")
    public String updateScoreWeight (@PathVariable("courseId") Long courseId,
                                     @RequestParam("quizWeight") double quizWeight,
                                     @RequestParam("examWeight") double examWeight,
                                     RedirectAttributes redirectAttributes) {
        try {
            scoreWeightService.saveScoreWeight(courseId, quizWeight, examWeight);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Score weight saved");
            return "redirect:/instructor/course/" + courseId + "/score_weight";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred, please try again later");
            return "redirect:/instructor/course/" + courseId + "/edit";
        }
    }
}
