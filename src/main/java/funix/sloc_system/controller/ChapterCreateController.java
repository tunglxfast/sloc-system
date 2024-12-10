package funix.sloc_system.controller;

import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import funix.sloc_system.service.ChapterService;
import funix.sloc_system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/instructor/course")
public class ChapterCreateController {
    @Autowired
    ChapterService chapterService;
    @Autowired
   CourseService courseService;

    @GetMapping(value = {"/{courseId}/chapters"})
    public String showCreateChapterPage(@PathVariable("courseId") Long courseId, Model model) {
        Course course = courseService.findById(courseId);
        model.addAttribute("course", course);
        model.addAttribute("chapters", chapterService.getChaptersByCourseId(courseId));
        model.addAttribute("newChapter", new Chapter());
        return "instructor/create_chapter";
    }

    @PostMapping("/{courseId}/chapters/add")
    public String addChapter(@PathVariable("courseId") Long courseId,
                             @ModelAttribute("newChapter") Chapter newChapter,
                             RedirectAttributes redirectAttributes) {
        chapterService.createChapter(courseId, newChapter.getTitle());
        redirectAttributes.addFlashAttribute("successMessage", "Chapter created successfully.");
        return String.format("redirect:/instructor/course/%d/chapters",courseId);
    }

    @PostMapping("/submit")
    public String submitChapters(@RequestParam Long courseId, RedirectAttributes redirectAttributes) {
        return String.format("redirect:/instructor/course/%d/edit",courseId);
    }
}
