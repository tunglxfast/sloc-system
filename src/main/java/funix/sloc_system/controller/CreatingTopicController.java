package funix.sloc_system.controller;

import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.service.ChapterService;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import funix.sloc_system.security.SecurityUser;

@Controller
@RequestMapping("/instructor/course/{courseId}/chapter/{chapterId}/topic")
public class CreatingTopicController {

    @Autowired
    private ChapterService chapterService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private TopicService topicService;

    @GetMapping("/create")
    public String showCreateTopic(@PathVariable Long courseId,
                                @PathVariable Long chapterId,
                                @RequestParam String type,
                                Model model) {
        try {
            CourseDTO courseDTO = courseService.getEditingCourseDTO(courseId);
            ChapterDTO chapterDTO = chapterService.getEditingChapterDTO(chapterId);
            model.addAttribute("course", courseDTO);
            model.addAttribute("chapter", chapterDTO);
            model.addAttribute("topicType", type);
            model.addAttribute("newTopic", new TopicDTO());
            return "instructor/create_topic";
        } catch (Exception e) {
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId + "?error=" + e.getMessage();
        }
    }

    @GetMapping("/{topicId}")
    public String showEditTopic(@PathVariable Long courseId,
                              @PathVariable Long chapterId,
                              @PathVariable Long topicId,
                              Model model) {
        try {
            CourseDTO courseDTO = courseService.getEditingCourseDTO(courseId);
            ChapterDTO chapterDTO = chapterService.getEditingChapterDTO(chapterId);
            TopicDTO topicDTO = topicService.getEditingTopicDTO(topicId);
            
            model.addAttribute("course", courseDTO);
            model.addAttribute("chapter", chapterDTO);
            model.addAttribute("topic", topicDTO);
            model.addAttribute("topicType", topicDTO.getTopicType());
            
            return "instructor/create_topic";
        } catch (Exception e) {
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/add")
    public String addTopic(@PathVariable Long courseId,
                          @PathVariable Long chapterId,
                          @ModelAttribute("newTopic") TopicDTO topicDTO,
                          RedirectAttributes redirectAttributes) {
        try {
            chapterService.addTopic(chapterId, topicDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Topic created successfully.");
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId;
        }
    }

    @PostMapping("/save")
    public String saveTopic(@PathVariable Long courseId,
                          @PathVariable Long chapterId,
                          @ModelAttribute("topic") TopicDTO topicDTO,
                          @AuthenticationPrincipal SecurityUser securityUser,
                          RedirectAttributes redirectAttributes) {
        try {
            topicService.saveTopicChanges(topicDTO, securityUser.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Topic updated successfully.");
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor/course/" + courseId + "/chapter/" + chapterId;
        }
    }
}
