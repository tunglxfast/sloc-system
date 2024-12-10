package funix.sloc_system.controller;

import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/instructor/chapter/{chapterId}/topic")
public class TopicCreateController {

    @Autowired
    private ChapterService chapterService;

    @GetMapping(value = {"","/"})
    public String showAddTopicPage(@PathVariable Long chapterId, Model model) {
        Chapter chapter = chapterService.findById(chapterId);
        model.addAttribute("chapter", chapter);
        model.addAttribute("newTopic", new Topic());
        return "instructor/add_topic";
    }

    @PostMapping("/add")
    public String addTopic(@PathVariable Long chapterId, @ModelAttribute("newTopic") Topic newTopic,
                           RedirectAttributes redirectAttributes) {
        chapterService.addTopic(chapterId, newTopic);
        redirectAttributes.addFlashAttribute("successMessage", "Topic created successfully.");
        return "redirect:/instructor/chapter/" + chapterId + "/topics";
    }
}
