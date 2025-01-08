package funix.sloc_system.controller;

import funix.sloc_system.dto.CommentDTO;
import funix.sloc_system.dto.TopicDiscussionDTO;
import funix.sloc_system.entity.User;
import funix.sloc_system.service.CommentService;
import funix.sloc_system.service.TopicDiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/topic-discussions")
public class TopicDiscussionController {

    @Autowired
    private TopicDiscussionService topicDiscussionService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/{topicId}")
    public String getDiscussionsByTopic(@PathVariable Long topicId, Model model) {
        List<TopicDiscussionDTO> discussions = topicDiscussionService.getDiscussionsByTopicId(topicId);
        model.addAttribute("discussions", discussions);
        model.addAttribute("topicId", topicId);
        return "topic_discussions";
    }

    @GetMapping("/view/{id}")
    public String viewDiscussion(@PathVariable Long id, Model model) {
        TopicDiscussionDTO discussion = topicDiscussionService.getDiscussionById(id);
        List<CommentDTO> comments = commentService.getCommentsByDiscussionId(id);
        model.addAttribute("discussion", discussion);
        model.addAttribute("comments", comments);
        return "view_discussion";
    }

    @GetMapping("/create/{topicId}")
    public String showCreateDiscussionForm(@PathVariable Long topicId, Model model) {
        model.addAttribute("topicId", topicId);
        return "create_discussion";
    }

    @PostMapping("/create/{topicId}")
    public String createDiscussion(
            @PathVariable Long topicId,
            @AuthenticationPrincipal User user,
            @RequestParam String title,
            @RequestParam String content) {
        topicDiscussionService.createDiscussion(topicId, user.getId(), title, content);
        return "redirect:/topic-discussions/" + topicId;
    }

    @PostMapping("/{id}/comment")
    public String addComment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestParam String content) {
        commentService.createComment(id, user.getId(), content);
        return "redirect:/topic-discussions/view/" + id;
    }

    @PostMapping("/update/{id}")
    public String updateDiscussion(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content) {
        TopicDiscussionDTO discussion = topicDiscussionService.updateDiscussion(id, title, content);
        return "redirect:/topic-discussions/view/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteDiscussion(@PathVariable Long id) {
        TopicDiscussionDTO discussion = topicDiscussionService.getDiscussionById(id);
        Long topicId = discussion.getTopicId();
        topicDiscussionService.deleteDiscussion(id);
        return "redirect:/topic-discussions/" + topicId;
    }

    @PostMapping("/comments/update/{id}")
    public String updateComment(
            @PathVariable Long id,
            @RequestParam String content) {
        CommentDTO comment = commentService.updateComment(id, content);
        return "redirect:/topic-discussions/view/" + comment.getTopicDiscussionId();
    }

    @PostMapping("/comments/delete/{id}")
    public String deleteComment(@PathVariable Long id) {
        CommentDTO comment = commentService.getCommentById(id);
        Long discussionId = comment.getTopicDiscussionId();
        commentService.deleteComment(id);
        return "redirect:/topic-discussions/view/" + discussionId;
    }
} 