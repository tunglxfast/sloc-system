package funix.sloc_system.controller;

import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.dto.UserDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.RoleType;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.mapper.UserMapper;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.UserService;
import funix.sloc_system.util.AppUtil;
import funix.sloc_system.util.CourseEditingHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

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

    @GetMapping(value = {"", "/", "/courses"})
    public String showInstructorManageList(@AuthenticationPrincipal SecurityUser securityUser,
                                        @RequestParam(required = false) String errorMessage,
                                        Model model, 
                                        RedirectAttributes redirectAttributes){
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
        model.addAttribute("user", userDTO);
        model.addAttribute("courseEditingHolders", courseEditingHolders);
        model.addAttribute("courses", courseDTOList);
        model.addAttribute("errorMessage", errorMessage);
        return "instructor/instructor_courses";
    }

    @GetMapping("/notifications")
    public String getNotifications(Principal principal, Model model) {
        User instructor = userService.findByUsername(principal.getName());
        List<Course> rejectedCourses = courseService.getInstructorRejectedCourses(instructor);
        model.addAttribute("rejectedCourses", rejectedCourses);
        return "instructor/notifications";
    }

    @GetMapping("/course/{courseId}/view")
    public String viewCourse(@PathVariable Long courseId,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        CourseDTO courseDTO;
        try {
            courseDTO = appUtil.getEditingCourseDTO(courseId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor/courses";
        }
        model.addAttribute("course", courseDTO);
        return "instructor/course_view";
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
            return "redirect:/instructor/courses";
        }
        if (chapterNumber < 1
            || topicNumber < 1
            || chapterNumber > courseDTO.getChapters().size()
            || topicNumber > courseDTO.getChapters().get(chapterNumber - 1).getTopics().size()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chapter or topic not found");
            return "redirect:/instructor/courses";
        }

        ChapterDTO chapterDTO = courseDTO.getChapters().get(chapterNumber - 1);
        TopicDTO topicDTO = chapterDTO.getTopics().get(topicNumber - 1);
        Topic nextTopic;
        String nextTopicUrl;

        try {
            nextTopic = appUtil.findNextTopic(topicDTO.getId()); 
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/instructor/courses";
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
}
