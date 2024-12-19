package funix.sloc_system.mapper;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.enums.CourseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public CourseDTO toDTO(Course course) {
        if (course == null) {
            return null;
        }

        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setThumbnailUrl(course.getThumbnailUrl());
        dto.setCategory(categoryMapper.toDTO(course.getCategory()));
        dto.setCreatedBy(userMapper.toDTO(course.getCreatedBy()));
        dto.setLastUpdatedBy(userMapper.toDTO(course.getLastUpdatedBy()));
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());
        dto.setStatus(course.getStatus().name());
        dto.setRejectReason(course.getRejectReason());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        dto.setChapters(course.getChapters().stream()
                .map(chapterMapper::toDTO)
                .toList());
        dto.setEnrollments(course.getEnrollments().stream()
                .map(enrollmentMapper::toDTO)
                .collect(Collectors.toSet()));
        dto.setInstructor(userMapper.toDTO(course.getInstructor()));
        
        return dto;
    }

    public Course toEntity(CourseDTO dto) {
        if (dto == null) {
            return null;
        }

        Course course = new Course();
        course.setId(dto.getId());
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setThumbnailUrl(dto.getThumbnailUrl());
        course.setCategory(categoryMapper.toEntity(dto.getCategory()));
        course.setCreatedBy(userMapper.toEntity(dto.getCreatedBy()));
        course.setLastUpdatedBy(userMapper.toEntity(dto.getLastUpdatedBy()));
        course.setStartDate(dto.getStartDate());
        course.setEndDate(dto.getEndDate());
        course.setStatus(CourseStatus.valueOf(dto.getStatus()));
        course.setRejectReason(dto.getRejectReason());
        course.setCreatedAt(dto.getCreatedAt());
        course.setUpdatedAt(dto.getUpdatedAt());
        
        if (dto.getChapters() != null) {
            course.setChapters(dto.getChapters().stream()
                    .map(chapterMapper::toEntity)
                    .toList());
        }
        
        if (dto.getEnrollments() != null) {
            course.setEnrollments(dto.getEnrollments().stream()
                    .map(enrollmentMapper::toEntity)
                    .collect(Collectors.toSet()));
        }
        
        course.setInstructor(userMapper.toEntity(dto.getInstructor()));
        
        return course;
    }
} 