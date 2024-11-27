package funix.sloc_system.controller;

import funix.sloc_system.entity.*;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.*;
import funix.sloc_system.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private ApplicationUtil appUtil;

    @Autowired
    private QuizService quizService;

    // xem tất cả khóa học
    @GetMapping(value = {"","/"})
    public String listCourses(Model model) {
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "courses";
    }

    // Xem giới thiệu sơ về khóa học
    @GetMapping(value = {"/{id}" ,"/{id}/general"})
    public String viewCourseGeneral(@PathVariable Long id, @AuthenticationPrincipal SecurityUser securityUser, Model model) {
        Course course = courseService.getCourseById(id);
        User user = securityUser.getUser();
        if (course != null && user != null) {
            boolean isEnrolled = enrollmentService.isEnrolled(user, course);
            model.addAttribute("course", course);
            model.addAttribute("isEnrolled", isEnrolled);
            return "course/general";
        } else {
            return "redirect:/courses";
        }
    }

    // đăng ký khóa học
    @GetMapping("/{courseId}/enroll")
    public String enrollCourse(@PathVariable Long courseId, @AuthenticationPrincipal SecurityUser securityUser, Model model) {
        Course course = courseService.getCourseById(courseId);
        User user = securityUser.getUser();
        if (course != null && user != null) {
            String response  = enrollmentService.enrollCourse(user, course);
            if (response.equalsIgnoreCase("Register successfully")) {
                return String.format("redirect:/courses/%d/1_1", courseId);
            } else {
                return String.format("redirect:/courses?error=%s",response);
            }
        } else {
            return "redirect:/courses";
        }
    }

    // xem chi tiết khóa học
    @GetMapping("/{courseId}/{chapterNumber}_{topicNumber}")
    public String viewCourseDetail(
            @PathVariable Long courseId,
            @PathVariable int chapterNumber,
            @PathVariable int topicNumber,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model) {

        User user = securityUser.getUser();
        Course course = courseService.getCourseById(courseId);
        if (course == null || user == null ) {
            return "redirect:/courses";
        }

        // Kiểm tra topic tồn tại
        Topic topic = topicService.getTopicByChapterAndTopicSequence(courseId, chapterNumber, topicNumber);
        // Kiểm tra đăng ký khóa học
        boolean isEnrolled = enrollmentService.isEnrolled(user, course);
        if (!isEnrolled || topic == null) {
            return String.format("redirect:/courses/%d", courseId);
        }

        Topic nextTopic = appUtil.findNextTopic(topic.getId());

        model.addAttribute("topic", topic);
        model.addAttribute("courseId", courseId);
        model.addAttribute("nextTopic", nextTopic);

        TopicType topicType = topic.getTopicType();
        if (topicType.equals(TopicType.READING) || topicType.equals(TopicType.VIDEO)){
            return "course/course_lesson";
        } else {
            return "course/course_quiz";
        }
    }

    @PostMapping("/{courseId}/submit")
    public String submitQuiz(@RequestParam Long quizId,
                             @RequestParam Map<String, String> answers,
                             Model model) {

        QuizResult result = quizService.calculateScore(quizId, answers);
        Quiz quiz = quizService.getQuizById(quizId);

        if (quiz != null) {
            model.addAttribute("result", result);
            model.addAttribute("topic", quiz);
            model.addAttribute("courseId", quiz.getChapter().getCourse().getId());

            Topic nextTopic = appUtil.findNextTopic(quiz.getId());
            model.addAttribute("nextTopic", nextTopic);

        }
        return "course/quiz";
    }
}
