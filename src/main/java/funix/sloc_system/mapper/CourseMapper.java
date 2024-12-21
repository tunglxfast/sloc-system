package funix.sloc_system.mapper;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
        if (course.getChapters() != null) {
            dto.setChapters(chapterMapper.toDTO(course.getChapters()));
        }
        if (course.getEnrollments() != null) {
            dto.setEnrollments(enrollmentMapper.toDTO(course.getEnrollments()));
        }
        if (course.getInstructor() != null) {
            dto.setInstructor(userMapper.toDTO(course.getInstructor()));
        }
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
        
        if (dto.getChapters() != null) {
            course.setChapters(chapterMapper.toEntity(dto.getChapters()));
        }
        
        if (dto.getEnrollments() != null) {
            course.setEnrollments(enrollmentMapper.toEntity(dto.getEnrollments()));
        }

        if (dto.getInstructor() != null) {
            course.setInstructor(userMapper.toEntity(dto.getInstructor()));
        }
        
        return course;
    }

    public List<CourseDTO> toDTO(List<Course> courses) {
        return courses.stream().map(this::toDTO).toList();
    }

    public List<Course> toEntity(List<CourseDTO> courseDTOList) {
        return courseDTOList.stream().map(this::toEntity).toList();
    }
} 