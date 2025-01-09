package funix.sloc_system.controller;

import funix.sloc_system.dto.CommentDTO;
import funix.sloc_system.dto.TopicDiscussionDTO;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.CommentService;
import funix.sloc_system.service.TopicDiscussionService;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.util.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/topic-discussions")
public class TopicDiscussionController {

    @Autowired
    private TopicDiscussionService topicDiscussionService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private AppUtil appUtil;

    // --- Constants ---
    private final String ERROR_MSG_KEY = "errorMessage";
    private final String ERROR_MSG_ACCESS_DENIED = "You are not allowed to access this discussion";

    @GetMapping("/view/{discussionId}")
    public String viewDiscussion(@PathVariable Long discussionId, 
                                @RequestParam(required = false) Boolean fromInstructor,
                                @AuthenticationPrincipal SecurityUser securityUser, 
                                Model model, 
                                RedirectAttributes redirectAttributes) {
        if (!checkCourseAccessAbilityByDiscussionId(discussionId, securityUser.getUserId())) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }

        try {
            setViewDiscussionModel(discussionId, fromInstructor, model);
            
            return "discussion/view_discussion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error occurred while viewing discussion");
            return "redirect:/courses/";
        }
    }

    @PostMapping("/create/{topicId}")
    public String createDiscussion(
            @PathVariable Long topicId,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam String title,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {
        if (!appUtil.checkCourseAccessAbilityByTopicId(topicId, securityUser.getUserId())) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }
        TopicDiscussionDTO discussion = topicDiscussionService.createDiscussion(topicId, securityUser.getUserId(), title, content);
        return "redirect:/topic-discussions/view/" + discussion.getId();
    }

    @PostMapping("/comments/create/{discussionId}")
    public String addComment(
            @PathVariable Long discussionId,
            @RequestParam(required = false) Boolean fromInstructor,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam String content,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (!checkCourseAccessAbilityByDiscussionId(discussionId, securityUser.getUserId())) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }
        commentService.createComment(discussionId, securityUser.getUserId(), content);

        if (fromInstructor != null) {
            redirectAttributes.addFlashAttribute("fromInstructor", fromInstructor);
        }
        return "redirect:/topic-discussions/view/" + discussionId;
    }

    @PostMapping("/update/{discussionId}")
    public String updateDiscussion(
            @PathVariable Long discussionId,
            @RequestParam(required = false) Boolean fromInstructor,
            @RequestParam String title,
            @RequestParam String content,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes) {
        if (!checkCourseAccessAbilityByDiscussionId(discussionId, securityUser.getUserId())) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }

        topicDiscussionService.updateDiscussion(discussionId, title, content);

        if (fromInstructor != null) {
            redirectAttributes.addFlashAttribute("fromInstructor", fromInstructor);
        }
        return "redirect:/topic-discussions/view/" + discussionId;
    }

    @PostMapping("/delete/{discussionId}")
    public String deleteDiscussion(@PathVariable Long discussionId,
                                @RequestParam(required = false) Boolean fromInstructor,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                RedirectAttributes redirectAttributes) {
        if (!checkCourseAccessAbilityByDiscussionId(discussionId, securityUser.getUserId())) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }
        try {
            Long courseId = topicDiscussionService.getCourseId(discussionId);
            TopicDiscussionDTO discussion = topicDiscussionService.getDiscussionById(discussionId);
            String discussionBackUrl = getDiscussionBackUrl(discussion);
            topicDiscussionService.deleteDiscussion(discussionId);

            if (fromInstructor != null && fromInstructor == true) {
                return String.format("redirect:/instructor/course/%d/discussions", courseId);
            } else {
                return "redirect:" + discussionBackUrl;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error occurred while deleting discussion");
            return "redirect:/courses/";
        }
    }

    @PostMapping("/comments/update/{commentId}")
    public String updateComment(
            @PathVariable Long commentId,
            @RequestParam String content,
            @RequestParam(required = false) Boolean fromInstructor,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes) {
        if (!checkCourseAccessAbilityByCommentId(commentId, securityUser.getUserId())) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }
        CommentDTO comment = commentService.updateComment(commentId, content);

        redirectAttributes.addFlashAttribute("successMessage", "Comment updated successfully");
        if (fromInstructor != null) {
            redirectAttributes.addFlashAttribute("fromInstructor", fromInstructor);
        }
        return "redirect:/topic-discussions/view/" + comment.getTopicDiscussionId();
    }

    @PostMapping("/comments/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam(required = false) Boolean fromInstructor,
                                @AuthenticationPrincipal SecurityUser securityUser,
                                RedirectAttributes redirectAttributes) {
        if (!checkCourseAccessAbilityByCommentId(commentId, securityUser.getUserId())) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }
        CommentDTO comment = commentService.getCommentById(commentId);
        Long discussionId = comment.getTopicDiscussionId();
        commentService.deleteComment(commentId);

        redirectAttributes.addFlashAttribute("successMessage", "Comment deleted successfully");
        if (fromInstructor != null) {
            redirectAttributes.addFlashAttribute("fromInstructor", fromInstructor);
        }
        return "redirect:/topic-discussions/view/" + discussionId;
    }

    // --- Helper methods ---

    // Get back url from discussion
    private String getDiscussionBackUrl(TopicDiscussionDTO discussion) {
        Long topicId = discussion.getTopicId();
        Topic topic = topicRepository.findById(topicId).orElse(null); 
        if (topic == null) {
          return "/courses/";
        }
        else {
          Long courseId = topic.getChapter().getCourse().getId();
          int chapterNumber = topic.getChapter().getSequence();
          int topicNumber = topic.getSequence();
          return String.format("/courses/%d/%d_%d", courseId, chapterNumber, topicNumber);
        }
    }

    /* 
     * Check if user has access to this discussion.
     * 
     * @param discussionId the id of the discussion
     * @param userId the id of the user
     * @return true if user has access to this discussion, false otherwise
     */
    private boolean checkCourseAccessAbilityByDiscussionId(Long discussionId, Long userId) {
        TopicDiscussionDTO discussion = topicDiscussionService.getDiscussionById(discussionId);
        Long topicId = discussion.getTopicId();
        Topic topic = topicRepository.findById(topicId).orElse(null);
        if (topic == null) {
            return false;
        }
        return appUtil.checkCourseAccessAbilityByTopicId(topicId, userId);
    }


    private boolean checkCourseAccessAbilityByCommentId(Long commentId, Long userId) {
        CommentDTO comment = commentService.getCommentById(commentId);
        Long discussionId = comment.getTopicDiscussionId();
        return checkCourseAccessAbilityByDiscussionId(discussionId, userId);
    }

    private void setViewDiscussionModel(Long discussionId, Boolean fromInstructor, Model model) {
        Long courseId = topicDiscussionService.getCourseId(discussionId);
        TopicDiscussionDTO discussion = topicDiscussionService.getDiscussionById(discussionId);
        String discussionBackUrl = getDiscussionBackUrl(discussion);
        List<CommentDTO> comments = commentService.getCommentsByDiscussionId(discussionId);
        model.addAttribute("discussion", discussion);
        model.addAttribute("comments", comments);


        boolean isFromInstructor = false;
        if (fromInstructor != null && fromInstructor == true) {
            isFromInstructor = true;
        } else if (model.getAttribute("fromInstructor") != null 
                && model.getAttribute("fromInstructor").equals(true)) {
            isFromInstructor = true;
        }

        if (isFromInstructor) {
            model.addAttribute("discussionBackUrl", 
                String.format("/instructor/course/%d/discussions", courseId));
            model.addAttribute("fromInstructor", isFromInstructor);
        } else {
            model.addAttribute("discussionBackUrl", discussionBackUrl);
        }
    }
} 
