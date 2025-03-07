package funix.sloc_system.controller;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.ChapterService;
import funix.sloc_system.util.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/instructor/course/{courseId}/edit/chapters")
public class CreatingChapterController {
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private AppUtil appUtil;

    @GetMapping(value = {"","/"})
    public String showChapterList(@PathVariable Long courseId,
                                  @RequestParam(value = "errorMessage", required = false) String errorMessage,
                                  @RequestParam(value = "successMessage", required = false) String successMessage,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        try {
            CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);
            model.addAttribute("courseTitle", courseDTO.getTitle());
            model.addAttribute("courseId", courseDTO.getId());
            model.addAttribute("chapters", courseDTO.getChapters());
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("successMessage", successMessage);
            return "instructor/edit_course_content";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor/courses";
        }
    }

    @PostMapping("/add")
    public String addChapter(@PathVariable Long courseId,
                             @RequestParam("title") String title,
                             @AuthenticationPrincipal SecurityUser securityUser,
                             RedirectAttributes redirectAttributes) {
        Long instructorId = securityUser.getUserId();
        try {
            chapterService.createChapter(courseId, title, instructorId);
            redirectAttributes.addFlashAttribute("successMessage", "Chapter created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chapter fail to create.");
        }
        return String.format("redirect:/instructor/course/%d/edit/chapters", courseId);
    }

    @PostMapping("/edit")
    public String saveChapter(@PathVariable Long courseId,
                              @RequestParam("chapterId") Long chapterId,
                              @RequestParam("title") String title,
                              @AuthenticationPrincipal SecurityUser securityUser,
                              RedirectAttributes redirectAttributes) {
        Long instructorId = securityUser.getUserId();
        try {
            chapterService.updateChapter(courseId, chapterId, title, instructorId);
            redirectAttributes.addFlashAttribute("successMessage", "Chapter updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return String.format("redirect:/instructor/course/%d/edit/chapters", courseId);
    }

    @GetMapping("/delete")
    public String deleteChapter(@PathVariable Long courseId,
                                @RequestParam("chapterId") Long chapterId,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                RedirectAttributes redirectAttributes) {
        Long instructorId = securityUser.getUserId();
        try {
            chapterService.deleteChapter(courseId, chapterId, instructorId);
            redirectAttributes.addFlashAttribute("successMessage", "Chapter deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error when deleting chapter content");
        }
        return "redirect:/instructor/course/" + courseId + "/edit/chapters";
    }
}
