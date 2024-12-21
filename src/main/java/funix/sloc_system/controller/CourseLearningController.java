package funix.sloc_system.controller;

import funix.sloc_system.dto.TestResultDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.entity.User;
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
public class CourseLearningController {
    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private ApplicationUtil appUtil;

    @Autowired
    private TestResultService testResultService;

    /**
     * list all courses
     * @param model
     * @return
     */
    @GetMapping(value = {"","/"})
    public String listCourses(Model model) {
        List<Course> courses = courseService.getAvailableCourses();
        model.addAttribute("courses", courses);
        return "courses";
    }

    /**
     * Course General View
     * @param id
     * @param securityUser
     * @param model
     * @return
     */
    @GetMapping(value = {"/{id}" ,"/{id}/general"})
    public String viewCourseGeneral(@PathVariable Long id, @AuthenticationPrincipal SecurityUser securityUser, Model model) {
        if (!appUtil.isCourseReady(id)) {
            return "redirect:/courses";
        }

        Course course = courseService.findById(id);
        User user = userService.findById(securityUser.getUserId());
        if (course != null && user != null) {
            boolean isEnrolled = enrollmentService.isEnrolled(user, course);
            model.addAttribute("course", course);
            model.addAttribute("isEnrolled", isEnrolled);
            return "course/general";
        } else {
            return "redirect:/courses";
        }
    }

    @GetMapping("/{courseId}/enroll")
    public String enrollCourse(@PathVariable Long courseId, @AuthenticationPrincipal SecurityUser securityUser, Model model) {
        if (!appUtil.isCourseReady(courseId)) {
            return "redirect:/courses";
        }

        Course course = courseService.findById(courseId);
        User user = userService.findById(securityUser.getUserId());
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

    @GetMapping("/{courseId}/{chapterNumber}_{topicNumber}")
    public String viewCourseDetail(
            @PathVariable Long courseId,
            @PathVariable int chapterNumber,
            @PathVariable int topicNumber,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model) {
        if (!appUtil.isCourseReady(courseId)) {
            return "redirect:/courses";
        }

        User user = userService.findById(securityUser.getUserId());
        Course course = courseService.findById(courseId);
        if (course == null || user == null ) {
            return "redirect:/courses";
        }
        // Check topic exist
        Topic topic = topicService.findByChapterAndTopicSequence(courseId, chapterNumber, topicNumber);
        // Check already enroll
        boolean isEnrolled = enrollmentService.isEnrolled(user, course);
        if (!isEnrolled || topic == null) {
            return String.format("redirect:/courses/%d", courseId);
        }

        Topic nextTopic = appUtil.findNextTopic(topic.getId());

        model.addAttribute("topic", topic);
        model.addAttribute("courseId", courseId);
        model.addAttribute("nextTopic", nextTopic);
        TestResultDTO testResult = testResultService.findByUserIdAndTopicId(user.getId(), topic.getId());
        if (testResult != null) {
            model.addAttribute("result", testResult);
        }
        TopicType topicType = topic.getTopicType();
        if (topicType.equals(TopicType.EXAM)){
            return "course/course_exam";
        } else if (topicType.equals(TopicType.QUIZ)) {
            return "course/course_quiz";
        } else {
            return "course/course_lesson";
        }
    }

    @PostMapping("{courseId}/quiz/submit")
    public String submitQuiz(@PathVariable Long courseId,
                             @RequestParam("quizId") Long quizId,
                             @RequestParam Map<String, String> answers,
                             @AuthenticationPrincipal SecurityUser securityUser,
                             Model model) {

        if (!appUtil.isCourseReady(courseId)) {
            return "redirect:/courses";
        }

        TestResultDTO result = testResultService.calculateScore(securityUser.getUserId(), quizId, answers);
        Topic topic = topicService.findById(quizId);

        if (topic != null) {
            model.addAttribute("result", result);
            model.addAttribute("topic", topic);
            model.addAttribute("courseId", courseId);

            Topic nextTopic = appUtil.findNextTopic(topic.getId());
            model.addAttribute("nextTopic", nextTopic);

        }
        return "course/course_quiz";
    }

    @PostMapping("/{courseId}/exam/submit")
    public String submitExam(@PathVariable Long courseId,
                             @RequestParam Long examId,
                             @RequestParam Map<String, String> answers,
                             @AuthenticationPrincipal SecurityUser securityUser,
                             Model model) {

        if (!appUtil.isCourseReady(courseId)) {
            return "redirect:/courses";
        }

        TestResultDTO result = testResultService.calculateScore(securityUser.getUserId(), examId, answers);
        Topic exam = topicService.findById(examId);

        if (exam != null) {
            model.addAttribute("result", result);
            model.addAttribute("topic", exam);
            model.addAttribute("courseId", courseId);

            Topic nextTopic = appUtil.findNextTopic(exam.getId());
            model.addAttribute("nextTopic", nextTopic);

        }
        return "course/course_exam";
    }
}
