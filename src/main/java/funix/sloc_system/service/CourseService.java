package funix.sloc_system.service;

import funix.sloc_system.entity.Course;
import funix.sloc_system.dao.CourseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    @Autowired
    private CourseDAO courseDAO;

    public List<Course> getAllCourses() {
        return courseDAO.findAll();
    }

    public Course getCourseById(Long id) {
        return courseDAO.findById(id).orElse(null);
    }

    public List<Course> getCoursesByCategory(Long categoryId) {
        return courseDAO.findByCategoryId(categoryId);
    }
}
