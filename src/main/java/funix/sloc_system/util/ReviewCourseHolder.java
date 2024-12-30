package funix.sloc_system.util;

import org.springframework.stereotype.Component;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.enums.ContentAction;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class ReviewCourseHolder {

    private CourseDTO course;
    private String action;

    public ReviewCourseHolder(CourseDTO courseDTO, String action) {
        this.course = courseDTO;
        this.action = action;
    }

    public ReviewCourseHolder(CourseDTO courseDTO, ContentAction action) {
        this.course = courseDTO;
        this.action = action.name();
    }

    
    // Getters and Setters
    public CourseDTO getCourse() {
        return course;
    }

    public void setCourse(CourseDTO course) {
        this.course = course;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}