package funix.sloc_system.mapper;

import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.User;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseMapper {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ChapterMapper chapterMapper;
    
    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private CourseRepository courseRepository;

    public CourseDTO toDTO(Course course) {
        if (course == null) {
            return null;
        }

        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setThumbnailUrl(course.getThumbnailUrl());
        if (course.getCategory() != null) {
            dto.setCategory(categoryMapper.toDTO(course.getCategory()));
        }
        if (course.getCreatedBy() != null) {
            dto.setCreatedBy(userMapper.toDTO(course.getCreatedBy()));
        }
        if (course.getLastUpdatedBy() != null) {
            dto.setLastUpdatedBy(userMapper.toDTO(course.getLastUpdatedBy()));
        }
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());
        dto.setContentStatus(course.getContentStatus().name());
        dto.setApprovalStatus(course.getApprovalStatus().name());
        dto.setRejectReason(course.getRejectReason());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        if (course.getChapters() != null && !course.getChapters().isEmpty()) {
            for (Chapter chapter : course.getChapters()) {
                dto.addChapter(chapterMapper.toDTO(chapter));
            }
        } else {
            dto.setChapters(new ArrayList<>());
        }

        if (course.getEnrollments() != null) {
            dto.setEnrollments(enrollmentMapper.toDTO(course.getEnrollments()));
        }

        if (course.getInstructor() != null) {
            dto.setInstructor(userMapper.toDTO(course.getInstructor()));
        }
        return dto;
    }

    public Course toEntity(CourseDTO dto, User instructor) {
        if (dto == null) {
            return null;
        }

        Course course;
        if (dto.getId() != null) {
            course = courseRepository.findById(dto.getId()).orElse(new Course());
        } else {
            course = new Course();
        }

        if (course.getId() == null && dto.getId() != null) {
            course.setId(dto.getId());
        }

        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setThumbnailUrl(dto.getThumbnailUrl());
        if (dto.getCategory() != null) {
            course.setCategory(categoryMapper.toEntity(dto.getCategory()));
        }
        if (dto.getCreatedBy() != null) {
            course.setCreatedBy(userMapper.toEntity(dto.getCreatedBy()));
        }
        if (dto.getLastUpdatedBy() != null) {
            course.setLastUpdatedBy(userMapper.toEntity(dto.getLastUpdatedBy()));
        }
        course.setStartDate(dto.getStartDate());
        course.setEndDate(dto.getEndDate());
        if (dto.getContentStatus() != null) {
            course.setContentStatus(ContentStatus.valueOf(dto.getContentStatus()));
        }
        if (dto.getApprovalStatus() != null) {
            course.setApprovalStatus(ApprovalStatus.valueOf(dto.getApprovalStatus()));
        }
        course.setRejectReason(dto.getRejectReason());
        course.setCreatedAt(dto.getCreatedAt());
        course.setUpdatedAt(dto.getUpdatedAt());
        
        if (dto.getChapters() != null && !dto.getChapters().isEmpty()) {
            course.getChapters().clear();
            for (ChapterDTO chapterDTO : dto.getChapters()) {
                course.addChapter(chapterMapper.toEntity(chapterDTO, course));
            }
        }
        
        if (dto.getEnrollments() != null) {
            course.setEnrollments(enrollmentMapper.toEntity(dto.getEnrollments()));
        }

        course.setInstructor(instructor);
        
        return course;
    }

    public List<CourseDTO> toDTO(List<Course> courses) {
        return courses.stream().map(course -> toDTO(course)).collect(Collectors.toList());
    }

    public List<Course> toEntity(List<CourseDTO> courseDTOList, User instructor) {
        return courseDTOList.stream().map(dto -> toEntity(dto, instructor)).collect(Collectors.toList());
    }
} 