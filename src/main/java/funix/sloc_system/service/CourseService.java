package funix.sloc_system.service;

import funix.sloc_system.entity.Course;
import funix.sloc_system.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public List<Course> getCoursesByCategory(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId);
    }
}
