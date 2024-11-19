package funix.sloc_system.controller;

import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
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
    @GetMapping("/")
    public String listCourses(Model model) {
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "courses/list";
    }

    // Xem giới thiệu sơ về khóa học
    @GetMapping("/{id}/general")
    public String viewCourseGeneral(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id);
        model.addAttribute("course", course);
        return "course/general";
    }

    // đăng ký khóa học
    @GetMapping("/{id}/enroll")
    public String enrollCourse(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        Course course = courseService.getCourseById(id);
        if (course != null && user != null) {
            enrollmentService.enrollCourse(user, course);
            return String.format("redirect:/courses/%d/topics", id);
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
