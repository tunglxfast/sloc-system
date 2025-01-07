package funix.sloc_system.controller;

import funix.sloc_system.dto.CommentDTO;
import funix.sloc_system.dto.TopicDiscussionDTO;
import funix.sloc_system.entity.User;
import funix.sloc_system.service.CommentService;
import funix.sloc_system.service.TopicDiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    // API endpoints for AJAX calls
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<TopicDiscussionDTO> updateDiscussion(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content) {
        TopicDiscussionDTO updatedDiscussion = topicDiscussionService.updateDiscussion(id, title, content);
        return ResponseEntity.ok(updatedDiscussion);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteDiscussion(@PathVariable Long id) {
        topicDiscussionService.deleteDiscussion(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/comments/{id}")
    @ResponseBody
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long id,
            @RequestParam String content) {
        CommentDTO updatedComment = commentService.updateComment(id, content);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/api/comments/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }
} 