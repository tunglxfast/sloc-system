package funix.sloc_system.controller;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.security.SecurityUser;
import funix.sloc_system.service.CourseService;
import funix.sloc_system.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    // xem tất cả khóa học
    @GetMapping(value = {"","/"})
    public String listCourses(Model model) {
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "courses";
    }

    // Xem giới thiệu sơ về khóa học
    @GetMapping(value = {"/{id}/" ,"/{id}/general"})
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
                return String.format("redirect:/courses/%d/topics", courseId);
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
            @PathVariable Integer chapterNumber,
            @PathVariable Integer topicNumber,
            @AuthenticationPrincipal User user,
            Model model) {

        Course course = courseService.getCourseById(courseId);
        // Kiểm tra xem đã đăng ký khóa học chưa
        Boolean isEnrolled = enrollmentService.isEnrolled(user, course);
        if (!isEnrolled) {
            return String.format("redirect:/courses/%d/general", courseId);
        }

        model.addAttribute("course", course);
        return "course/detail";
    }
}
