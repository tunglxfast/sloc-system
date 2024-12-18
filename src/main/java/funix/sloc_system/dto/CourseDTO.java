package funix.sloc_system.dto;

import funix.sloc_system.entity.Role;
import funix.sloc_system.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private CategoryDTO category;
    private UserDTO createdBy;
    private UserDTO lastUpdatedBy;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate endDate;
    private String status;
    private String rejectReason;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate createdAt;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate updatedAt;
    private List<ChapterDTO> chapters;
    private Set<EnrollmentDTO> enrollments;
    private UserDTO instructor;

    // handy methods
    // not overwrite id, createAt, updateAt
    public void updateCourseDTO(CourseDTO otherCourse) {
        if (otherCourse.getTitle() != null) {
            this.setTitle(otherCourse.getTitle());
        }
        if (otherCourse.getDescription() != null) {
            this.setDescription(otherCourse.getDescription());
        }
        if (otherCourse.getThumbnailUrl() != null) {
            this.setThumbnailUrl(otherCourse.getThumbnailUrl());
        }
        if (otherCourse.getCategory() != null) {
            this.setCategory(otherCourse.getCategory());
        }
        if (otherCourse.getCreatedBy() != null) {
            this.setCreatedBy(otherCourse.getCreatedBy());
        }
        if (otherCourse.getLastUpdatedBy() != null) {
            this.setLastUpdatedBy(otherCourse.getLastUpdatedBy());
        }
        if (otherCourse.getChapters() != null) {
            this.setChapters(otherCourse.getChapters());
        }
        if (otherCourse.getEnrollments() != null && !otherCourse.getEnrollments().isEmpty()) {
            this.setEnrollments(otherCourse.getEnrollments());
        }
        if (otherCourse.getInstructor() != null) {
            this.setInstructor(otherCourse.getInstructor());
        }
        if (otherCourse.getStartDate() != null) {
            this.setStartDate(otherCourse.getStartDate());
        }
        if (otherCourse.getEndDate() != null) {
            this.setEndDate(otherCourse.getEndDate());
        }
        if (otherCourse.getStatus() != null) {
            this.setStatus(otherCourse.getStatus());
        }
        if (otherCourse.getRejectReason() != null) {
            this.setRejectReason(otherCourse.getRejectReason());
        }
    }

    public void setCreatedBy(UserDTO userDTO) {
        this.createdBy = userDTO;
    }

    public void setCreatedBy(User user) {
        if (user != null) {
            Set<RoleDTO> roles = new HashSet<>();
            for (Role role: user.getRoles()){
                roles.add(new RoleDTO(role.getId(), role.getName()));
            }
            UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), roles);
            this.setCreatedBy(userDTO);
        }
    }

    public void setLastUpdatedBy(UserDTO userDTO) {
        this.lastUpdatedBy = userDTO;
    }

    public void setLastUpdatedBy(User user) {
        if (user != null) {
            Set<RoleDTO> roles = new HashSet<>();
            for (Role role: user.getRoles()){
                roles.add(new RoleDTO(role.getId(), role.getName()));
            }
            UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), roles);
            this.setLastUpdatedBy(userDTO);
        }
    }

    public void setInstructor(UserDTO userDTO) {
        this.instructor = userDTO;
    }

    public void setInstructor(User user) {
        if (user != null) {
            Set<RoleDTO> roles = new HashSet<>();
            for (Role role: user.getRoles()){
                roles.add(new RoleDTO(role.getId(), role.getName()));
            }
            UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), roles);
            this.setInstructor(userDTO);
        }
    }


}
