package funix.sloc_system.controller;

import funix.sloc_system.dto.CommentDTO;
import funix.sloc_system.dto.TopicDiscussionDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.entity.TopicDiscussion;
import funix.sloc_system.entity.User;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.CommentService;
import funix.sloc_system.service.TopicDiscussionService;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.repository.UserRepository;
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
    private UserRepository userRepository;

    // --- Constants ---
    private final String ERROR_MSG_KEY = "errorMessage";
    private final String ERROR_MSG_ACCESS_DENIED = "You are not allowed to access this discussion";

    // @GetMapping("/{topicId}")
    // public String getDiscussionsByTopic(@PathVariable Long topicId, Model model) {
    //     List<TopicDiscussionDTO> discussions = topicDiscussionService.getDiscussionsByTopicId(topicId);
    //     model.addAttribute("discussions", discussions);
    //     model.addAttribute("topicId", topicId);
    //     return "discussion/topic_discussions";
    // }

    @GetMapping("/view/{discussionId}")
    public String viewDiscussion(@PathVariable Long discussionId, 
                                @AuthenticationPrincipal SecurityUser securityUser, 
                                Model model, 
                                RedirectAttributes redirectAttributes) {
        if (!checkAccessAbility(discussionId, securityUser)) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }

        TopicDiscussionDTO discussion = topicDiscussionService.getDiscussionById(discussionId);
        String discussionBackUrl = getDiscussionBackUrl(discussion);
        List<CommentDTO> comments = commentService.getCommentsByDiscussionId(discussionId);
        model.addAttribute("discussion", discussion);
        model.addAttribute("comments", comments);
        model.addAttribute("discussionBackUrl", discussionBackUrl);
        return "discussion/view_discussion";
    }

    @PostMapping("/create/{topicId}")
    public String createDiscussion(
            @PathVariable Long topicId,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam String title,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {
        if (!checkAccessAbility(topicId, securityUser)) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }
        TopicDiscussionDTO discussion = topicDiscussionService.createDiscussion(topicId, securityUser.getUserId(), title, content);
        return "redirect:/topic-discussions/view/" + discussion.getId();
    }

    @PostMapping("/comments/create/{discussionId}")
    public String addComment(
            @PathVariable Long discussionId,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {
        if (!checkAccessAbility(discussionId, securityUser)) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }
        commentService.createComment(discussionId, securityUser.getUserId(), content);
        return "redirect:/topic-discussions/view/" + discussionId;
    }

    @PostMapping("/update/{discussionId}")
    public String updateDiscussion(
            @PathVariable Long discussionId,
            @RequestParam String title,
            @RequestParam String content,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes) {
        if (!checkAccessAbility(discussionId, securityUser)) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }
        topicDiscussionService.updateDiscussion(discussionId, title, content);
        return "redirect:/topic-discussions/view/" + discussionId;
    }

    @PostMapping("/delete/{discussionId}")
    public String deleteDiscussion(@PathVariable Long discussionId,
                                  @AuthenticationPrincipal SecurityUser securityUser,
                                  RedirectAttributes redirectAttributes) {
        if (!checkAccessAbility(discussionId, securityUser)) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }
        TopicDiscussionDTO discussion = topicDiscussionService.getDiscussionById(discussionId);
        String discussionBackUrl = getDiscussionBackUrl(discussion);
        topicDiscussionService.deleteDiscussion(discussionId);
        return "redirect:" + discussionBackUrl;
    }

    @PostMapping("/comments/update/{commentId}")
    public String updateComment(
            @PathVariable Long commentId,
            @RequestParam String content,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes) {
        if (!checkAccessAbility(commentId, securityUser)) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }
        CommentDTO comment = commentService.updateComment(commentId, content);
        return "redirect:/topic-discussions/view/" + comment.getTopicDiscussionId();
    }

    @PostMapping("/comments/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                               @AuthenticationPrincipal SecurityUser securityUser,
                               RedirectAttributes redirectAttributes) {
        if (!checkAccessAbility(commentId, securityUser)) {
            redirectAttributes.addFlashAttribute(ERROR_MSG_KEY, ERROR_MSG_ACCESS_DENIED);
            return "redirect:/courses/";
        }
        CommentDTO comment = commentService.getCommentById(commentId);
        Long discussionId = comment.getTopicDiscussionId();
        commentService.deleteComment(commentId);
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

    // Check if user has access to this feature
    private boolean checkAccessAbility(Long discussionId, SecurityUser securityUser) {
        Long userId = securityUser.getUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        TopicDiscussion discussion = topicDiscussionService.getTopicDiscussionEntity(discussionId);
        if (discussion == null) {
            return false;
        }

        // is user enrolled in the course or is instructor of the course
        Course course = discussion.getTopic().getChapter().getCourse();
        if (course == null) {
            return false;
        }   
        
        boolean isEnrolled = course.getEnrollments().stream().anyMatch(enrollment -> enrollment.getUser().getId().equals(userId));
        boolean isInstructor = course.getInstructor().getId().equals(userId);
        return isEnrolled || isInstructor;
    }
} 
