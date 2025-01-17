package funix.sloc_system.mapper;

import funix.sloc_system.dto.EnrollmentDTO;
import funix.sloc_system.entity.Enrollment;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EnrollmentMapper {
    
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;

    public EnrollmentDTO toDTO(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setUser(userMapper.toDTO(enrollment.getUser()));
        dto.setCourseId(enrollment.getCourse().getId());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        
        return dto;
    }

    public Enrollment toEntity(EnrollmentDTO dto) {
        if (dto == null) {
            return null;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setId(dto.getId());
        enrollment.setUser(userRepository.findById(dto.getUser().getId()).orElse(null));
        enrollment.setCourse(courseRepository.findById(dto.getCourseId()).orElse(null));
        enrollment.setEnrollmentDate(dto.getEnrollmentDate());
        
        return enrollment;
    }

    public Set<EnrollmentDTO> toDTO(Set<Enrollment> enrollments) {
        return enrollments.stream().map(this::toDTO).collect(Collectors.toSet());
    }

    public Set<Enrollment> toEntity(Set<EnrollmentDTO> enrollmentDTOList) {
        return enrollmentDTOList.stream().map(this::toEntity).collect(Collectors.toSet());
    }
} 