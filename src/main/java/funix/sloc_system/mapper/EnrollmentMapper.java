package funix.sloc_system.mapper;

import funix.sloc_system.dto.EnrollmentDTO;
import funix.sloc_system.entity.Enrollment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
    
    @Autowired
    private UserMapper userMapper;

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
        enrollment.setUser(userMapper.toEntity(dto.getUser()));
        // Note: Course should be set separately as we only have the ID
        enrollment.setEnrollmentDate(dto.getEnrollmentDate());
        
        return enrollment;
    }
} 