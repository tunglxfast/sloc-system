package funix.sloc_system.controller;

import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.mapper.ChapterMapper;
import funix.sloc_system.service.ChapterService;
import funix.sloc_system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/instructor/course/{courseId}/chapter")
public class CreatingChapterController {
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private ChapterMapper chapterMapper;

    @GetMapping("")
    public String showChapterList(@PathVariable Long courseId,
                                  @ModelAttribute("errorMessage") String errorMessage,
                                  @ModelAttribute("successMessage") String successMessage,
                                  Model model) {
        try {
            CourseDTO courseDTO = courseService.getEditingCourseDTO(courseId);
            List<ChapterDTO> chapterDTOs = chapterMapper.toDTO(chapterService.findByCourseId(courseId));
            model.addAttribute("course", courseDTO);
            model.addAttribute("chapters", chapterDTOs);
            model.addAttribute("newChapter", new ChapterDTO());
            model.addAttribute("selectedChapter", null);
            return "instructor/edit_course_content";
        } catch (Exception e) {
            return "redirect:/instructor/courses?error=" + e.getMessage();
        }
    }

    @GetMapping("/{chapterId}")
    public String showChapterDetails(@PathVariable Long courseId, 
                                   @PathVariable Long chapterId, 
                                   Model model) {
        try {
            CourseDTO courseDTO = courseService.getEditingCourseDTO(courseId);
            ChapterDTO selectedChapter = chapterService.getEditingChapterDTO(chapterId);
            model.addAttribute("course", courseDTO);
            model.addAttribute("selectedChapter", selectedChapter);
            return "instructor/edit_course_content";
        } catch (Exception e) {
            return "redirect:/instructor/courses?error=" + e.getMessage();
        }
    }

    @PostMapping("/add")
    public String addChapter(@PathVariable Long courseId,
                           @RequestParam("title") String title,
                           RedirectAttributes redirectAttributes) {
        try {
            chapterService.createChapter(courseId, title);
            redirectAttributes.addFlashAttribute("successMessage", "Chapter created successfully.");
            return "redirect:/instructor/course/" + courseId + "/chapter";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chapter fail to create.");
            return "redirect:/instructor/course/" + courseId + "/chapter";
        }
    }

    @PostMapping("/save")
    public String saveChapter(@PathVariable Long courseId,
                            @ModelAttribute("chapter") ChapterDTO chapterDTO,
                            RedirectAttributes redirectAttributes) {
        try {
            chapterService.saveChapterChanges(chapterDTO, courseId);
            redirectAttributes.addFlashAttribute("successMessage", "Chapter updated successfully.");
            return "redirect:/instructor/course/" + courseId + "/chapter";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor/course/" + courseId + "/chapter";
        }
    }
}
