package funix.sloc_system.controller;

import funix.sloc_system.dto.CommentDTO;
import funix.sloc_system.dto.TopicDiscussionDTO;
import funix.sloc_system.entity.User;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.CommentService;
import funix.sloc_system.service.TopicDiscussionService;
import funix.sloc_system.util.AppUtil;
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

    @Autowired
    private AppUtil appUtil;

    // @GetMapping("/{topicId}")
    // public String getDiscussionsByTopic(@PathVariable Long topicId, Model model) {
    //     List<TopicDiscussionDTO> discussions = topicDiscussionService.getDiscussionsByTopicId(topicId);
    //     model.addAttribute("discussions", discussions);
    //     model.addAttribute("topicId", topicId);
    //     return "discussion/topic_discussions";
    // }

    @GetMapping("/view/{discussionId}")
    public String viewDiscussion(@PathVariable Long discussionId, Model model) {
        TopicDiscussionDTO discussion = topicDiscussionService.getDiscussionById(discussionId);
        String discussionBackUrl = appUtil.getDiscussionBackUrl(discussion);
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
            @RequestParam String content) {
        TopicDiscussionDTO discussion = topicDiscussionService.createDiscussion(topicId, securityUser.getUserId(), title, content);
        return "redirect:/topic-discussions/view/" + discussion.getId();
    }

    @PostMapping("/comments/create/{discussionId}")
    public String addComment(
            @PathVariable Long discussionId,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam String content) {
        commentService.createComment(discussionId, securityUser.getUserId(), content);
        return "redirect:/topic-discussions/view/" + discussionId;
    }

    @PostMapping("/update/{discussionId}")
    public String updateDiscussion(
            @PathVariable Long discussionId,
            @RequestParam String title,
            @RequestParam String content) {
        topicDiscussionService.updateDiscussion(discussionId, title, content);
        return "redirect:/topic-discussions/view/" + discussionId;
    }

    @PostMapping("/delete/{discussionId}")
    public String deleteDiscussion(@PathVariable Long discussionId) {
        TopicDiscussionDTO discussion = topicDiscussionService.getDiscussionById(discussionId);
        String discussionBackUrl = appUtil.getDiscussionBackUrl(discussion);
        topicDiscussionService.deleteDiscussion(discussionId);
        return "redirect:" + discussionBackUrl;
    }

    @PostMapping("/comments/update/{commentId}")
    public String updateComment(
            @PathVariable Long commentId,
            @RequestParam String content) {
        CommentDTO comment = commentService.updateComment(commentId, content);
        return "redirect:/topic-discussions/view/" + comment.getTopicDiscussionId();
    }

    @PostMapping("/comments/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId) {
        CommentDTO comment = commentService.getCommentById(commentId);
        Long discussionId = comment.getTopicDiscussionId();
        commentService.deleteComment(commentId);
        return "redirect:/topic-discussions/view/" + discussionId;
    }
} 