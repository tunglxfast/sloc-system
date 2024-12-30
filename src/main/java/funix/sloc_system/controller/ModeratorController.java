package funix.sloc_system.controller;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.service.ModeratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/moderator")
@PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
public class ModeratorController {

    @Autowired
    private ModeratorService moderatorService;

    @GetMapping(value = {"", "/"})
    public String showModeratorDashboard(Model model) {
        List<CourseDTO> pendingCourses = moderatorService.getPendingReviewCourses();
        model.addAttribute("pendingCourses", pendingCourses);
        return "moderator/dashboard";
    }

    @GetMapping("/course/{courseId}/review")
    public String showCourseReview(@PathVariable Long courseId, Model model) {
        CourseDTO courseDTO = moderatorService.getCourseForReview(courseId);
        model.addAttribute("course", courseDTO);
        return "moderator/course_review";
    }

    @PostMapping("/course/{courseId}/approve")
    public String approveCourse(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        try {
            moderatorService.approveCourse(courseId);
            redirectAttributes.addFlashAttribute("successMessage", "Course has been approved successfully.");
            return "redirect:/moderator";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving course: " + e.getMessage());
            return "redirect:/moderator";
        }
    }

    @PostMapping("/course/{courseId}/reject")
    public String rejectCourse(
            @PathVariable Long courseId,
            @RequestParam String rejectionReason,
            RedirectAttributes redirectAttributes) {
        try {
            moderatorService.rejectCourse(courseId, rejectionReason);
            redirectAttributes.addFlashAttribute("successMessage", "Course has been rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting course: " + e.getMessage());
        }
        return "redirect:/moderator";
    }

    @GetMapping("/course/{courseId}/chapter/{chapterNumber}/topic/{topicNumber}")
    public String showTopicReview(
            @PathVariable Long courseId,
            @PathVariable int chapterNumber,
            @PathVariable int topicNumber,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            CourseDTO courseDTO = moderatorService.getCourseForReview(courseId);
            
            // Find the chapter and topic using service methods
            ChapterDTO chapterDTO = moderatorService.findChapterBySequence(courseDTO, chapterNumber);
            TopicDTO topicDTO = moderatorService.findTopicBySequence(chapterDTO, topicNumber);
            
            model.addAttribute("course", courseDTO);
            model.addAttribute("chapter", chapterDTO);
            model.addAttribute("topic", topicDTO);
            
            return "moderator/topic_review";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/moderator/course/" + courseId + "/review";
        }
    }
}
