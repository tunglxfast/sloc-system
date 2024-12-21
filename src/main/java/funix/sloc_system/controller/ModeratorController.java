package funix.sloc_system.controller;

import funix.sloc_system.entity.Course;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/moderator")
public class ModeratorController {

    @Autowired
    private CourseService courseService;

    @PostMapping("/courses/{id}/approve")
    public String approveCourse(@PathVariable Long id) {
        Course course = courseService.findById(id);
        if (course == null) {
            return "moderator_courses";
        }
        course.setContentStatus(ContentStatus.PUBLISHED);
        course.setApprovalStatus(ApprovalStatus.APPROVED);
        course.setRejectReason(null);
        courseService.save(course);
        // Gửi thông báo (thêm logic gửi email)
        courseService.sendApproveEmail(course.getInstructor(), course);
        return "redirect:/moderator/courses";
    }

    @PostMapping("/courses/{id}/reject_all")
    public String rejectCourse(@PathVariable Long id, @RequestParam String reason) {
        Course course = courseService.findById(id);
        if (course == null) {
            return "moderator_courses";
        }
        if (course.getContentStatus() == ContentStatus.READY_TO_REVIEW
                || course.getContentStatus() == ContentStatus.DRAFT) {
            course.setContentStatus(ContentStatus.DRAFT);
        } else {
            course.setContentStatus(ContentStatus.PUBLISHED);
        }
        course.setApprovalStatus(ApprovalStatus.REJECTED);
        course.setRejectReason(reason);
        courseService.save(course);
        // Gửi thông báo qua email và UI
        courseService.sendRejectionEmail(course.getInstructor(), course, reason);
        return "redirect:/moderator/courses";
    }

}
