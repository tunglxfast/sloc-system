package funix.sloc_system.controller;

import funix.sloc_system.entity.Course;
import funix.sloc_system.service.ChapterService;
import funix.sloc_system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/instructor/chapter")
public class ChapterCreateController {
    @Autowired
    ChapterService chapterService;
    @Autowired
   CourseService courseService;

    @GetMapping(value = {"/create"})
    public String showCreateChapterForm(@RequestParam("courseId") Long courseId, Model model) {
        Course course = courseService.findById(courseId);
        model.addAttribute("course", course);
        model.addAttribute("chapters", course.getChapters());
        return "create_chapter";
    }

    @PostMapping("/save_draft")
    public String saveDraftChapters(@RequestParam Long courseId,
                                   @RequestParam List<String> titles,
                                   @RequestParam List<Long> chapterIds,
                                   RedirectAttributes redirectAttributes) {
        try {
            chapterService.saveOrUpdateChapters(courseId, titles, chapterIds);
            redirectAttributes.addFlashAttribute("successMessage", "Chapters saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return String.format("redirect:/instructor/chapter_create?courseId=%s", courseId);
    }

    @PostMapping("/submit")
    public String submitChapters(@RequestParam Long courseId, RedirectAttributes redirectAttributes) {
        try {
            courseService.submitForReview(courseId);
            redirectAttributes.addFlashAttribute("successMessage", "Course submitted for review.");
            return "redirect:/instructor/courses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return String.format("redirect:/instructor/chapter_create?courseId=%s", courseId);
        }
    }
}
