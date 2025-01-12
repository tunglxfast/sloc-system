package funix.sloc_system.controller;

import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.dto.RankingDTO;
import funix.sloc_system.dto.TopicDiscussionDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.entity.User;
import funix.sloc_system.entity.Ranking;
import funix.sloc_system.enums.RoleType;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.mapper.UserMapper;
import funix.sloc_system.mapper.RankingMapper;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.RankingService;
import funix.sloc_system.service.UserService;
import funix.sloc_system.service.TopicDiscussionService;
import funix.sloc_system.util.AppUtil;
import funix.sloc_system.util.CourseDiscussionStatsHolder;
import funix.sloc_system.util.CourseEditingHolder;
import funix.sloc_system.util.RedirectUrlHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
@RequestMapping("/instructor")
public class InstructorViewController {
    @Autowired
    private UserService userService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private AppUtil appUtil;
    @Autowired
    private TopicDiscussionService topicDiscussionService;
    @Autowired
    private RankingService rankingService;
    @Autowired
    private RankingMapper rankingMapper;

    @GetMapping(value = {"", "/", "/courses"})
    public String showDashboard(@AuthenticationPrincipal SecurityUser securityUser,
                                Model model){
        User user = userService.findById(securityUser.getUserId());
        UserDTO userDTO = userMapper.toDTO(user);
        List<Course> courseList;
        if (user.getStringRoles().contains(RoleType.MODERATOR.name())) {
            courseList = courseService.getAllCourses();
        } else {
            courseList = courseService.findByInstructor(user);
        }

        List<CourseDTO> courseDTOList = courseMapper.toDTO(courseList);
        List<CourseEditingHolder> courseEditingHolders = appUtil.getCourseEditingHolders(courseDTOList);
        
        // Get discussion stats for each course
        List<CourseDiscussionStatsHolder> courseDiscussionStats = getCourseDiscussionStats(courseDTOList);

        model.addAttribute("user", userDTO);
        model.addAttribute("courseEditingHolders", courseEditingHolders);
        model.addAttribute("courses", courseDTOList);
        model.addAttribute("courseDiscussionStats", courseDiscussionStats);
        return "instructor/instructor_dashboard";
    }

    @GetMapping("/course/{courseId}/view")
    public String viewCourse(@PathVariable Long courseId,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        CourseDTO courseDTO;
        try {
            courseDTO = appUtil.getEditingCourseDTO(courseId);

            List<Ranking> rankings = rankingService.getRankingsByCourse(courseId);
            List<RankingDTO> rankingDTOs = rankingMapper.toDTOs(rankings);
            model.addAttribute("rankings", rankingDTOs);
            model.addAttribute("course", courseDTO);
            return "instructor/course_view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return RedirectUrlHelper.REDIRECT_INSTRUCTOR_DASHBOARD;
        }
    }

    @GetMapping("/course/{courseId}/{chapterNumber}_{topicNumber}/view")
    public String viewCourseContent(@PathVariable Long courseId,
                                   @PathVariable int chapterNumber,
                                   @PathVariable int topicNumber,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        CourseDTO courseDTO;
        try {
            courseDTO = appUtil.getEditingCourseDTO(courseId);
        } catch (Exception e) { 
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return RedirectUrlHelper.REDIRECT_INSTRUCTOR_DASHBOARD;
        }
        if (chapterNumber < 1
            || topicNumber < 1
            || chapterNumber > courseDTO.getChapters().size()
            || topicNumber > courseDTO.getChapters().get(chapterNumber - 1).getTopics().size()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chapter or topic not found");
            return RedirectUrlHelper.REDIRECT_INSTRUCTOR_DASHBOARD;
        }

        ChapterDTO chapterDTO = courseDTO.getChapters().get(chapterNumber - 1);
        TopicDTO topicDTO = chapterDTO.getTopics().get(topicNumber - 1);
        Topic nextTopic;
        String nextTopicUrl;

        try {
            nextTopic = appUtil.findNextTopic(topicDTO.getId()); 
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return RedirectUrlHelper.REDIRECT_INSTRUCTOR_DASHBOARD;
        }

        if (nextTopic == null) {
            nextTopicUrl = "/instructor/course/" + courseId + "/view";
        } else {
            int nextChapterNumber = nextTopic.getChapter().getSequence();
            int nextTopicNumber = nextTopic.getSequence();
            nextTopicUrl = String.format("/instructor/course/%d/%d_%d/view", courseId, nextChapterNumber, nextTopicNumber);
        }

        model.addAttribute("course", courseDTO);
        model.addAttribute("chapter", chapterDTO);
        model.addAttribute("topic", topicDTO);
        model.addAttribute("nextTopicUrl", nextTopicUrl);
        return "instructor/topic_view";
    }

    @GetMapping("/course/{courseId}/discussions")
    public String viewCourseDiscussions(@PathVariable Long courseId,
                                    @AuthenticationPrincipal SecurityUser securityUser,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        CourseDTO courseDTO;
        try {
            courseDTO = appUtil.getEditingCourseDTO(courseId);
            courseDTO = appUtil.removeUnnecessaryTopicTypes(
                                    courseDTO, 
                                    List.of(TopicType.QUIZ.name(), 
                                    TopicType.EXAM.name()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return RedirectUrlHelper.REDIRECT_INSTRUCTOR_DASHBOARD;
        }

        Map<Long, List<TopicDiscussionDTO>> discussionsByTopic = getDiscussionsByTopic(courseId);

        model.addAttribute("course", courseDTO);
        model.addAttribute("discussionsByTopic", discussionsByTopic);
        return "instructor/course_discussions";
    }

    // --- Helper methods ---

    // Get discussions by topic
    private Map<Long, List<TopicDiscussionDTO>> getDiscussionsByTopic(Long courseId) {
        List<TopicDiscussionDTO> discussions = topicDiscussionService.getDiscussionsByCourseId(courseId);
        Map<Long, List<TopicDiscussionDTO>> discussionsByTopic = new HashMap<>();
        for (TopicDiscussionDTO discussion : discussions) {
            Long topicId = discussion.getTopicId();
            if (discussionsByTopic.containsKey(topicId)) {
                discussionsByTopic.get(topicId).add(discussion);
            } else {
                discussionsByTopic.put(topicId, new ArrayList<>());
                discussionsByTopic.get(topicId).add(discussion);
            }
        }
        return discussionsByTopic;
    }

    // Get course discussion stats
    private List<CourseDiscussionStatsHolder> getCourseDiscussionStats(List<CourseDTO> courseDTOList) {
        List<CourseDiscussionStatsHolder> courseDiscussionStats = new ArrayList<>();
        for (CourseDTO course : courseDTOList) {
            int topicCount = course.getChapters().stream()
                    .mapToInt(chapter -> chapter.getTopics().size())
                    .sum();
            List<TopicDiscussionDTO> discussions = topicDiscussionService.getDiscussionsByCourseId(course.getId());
            CourseDiscussionStatsHolder stats = new CourseDiscussionStatsHolder(
                course.getId(),
                course.getTitle(),
                course.getCategory().getName(),
                topicCount,
                discussions.size()
            );
            courseDiscussionStats.add(stats);
        }
        return courseDiscussionStats;
    }
}
