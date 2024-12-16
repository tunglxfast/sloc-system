package funix.sloc_system.dto;

import funix.sloc_system.enums.CourseStatus;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private Long category; // Category id
    private Long createdBy; // User id
    private Long lastUpdatedBy; // User id
    private LocalDate startDate;
    private LocalDate endDate;
    private String status = CourseStatus.DRAFT.name();
    private String rejectReason;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Set<Long> chapters = new HashSet<>(); // Set of Chapter id
    private Set<Long> enrollments = new HashSet<>(); // Set of Enrollment id
    private Set<Long> instructors = new HashSet<>(); // Set of Instructor id

    // Constructor
    public CourseDTO() {
    }

    public CourseDTO(Long id,
                     String title,
                     String description,
                     String thumbnailUrl,
                     Long category,
                     Long createdBy,
                     Long lastUpdatedBy,
                     LocalDate startDate,
                     LocalDate endDate,
                     String status,
                     String rejectReason,
                     LocalDate createdAt,
                     LocalDate updatedAt,
                     Set<Long> chapters,
                     Set<Long> enrollments,
                     Set<Long> instructors) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.category = category;
        this.createdBy = createdBy;
        this.lastUpdatedBy = lastUpdatedBy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.chapters = chapters;
        this.enrollments = enrollments;
        this.instructors = instructors;
    }

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
        if (otherCourse.getInstructors() != null && !otherCourse.getInstructors().isEmpty()) {
            this.setInstructors(otherCourse.getInstructors());
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getCategory() {
        return category;
    }

    public void setCategory(Long category) {
        this.category = category;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Long> getChapters() {
        return chapters;
    }

    public void setChapters(Set<Long> chapters) {
        this.chapters = chapters;
    }

    public Set<Long> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(Set<Long> enrollments) {
        this.enrollments = enrollments;
    }

    public Set<Long> getInstructors() {
        return instructors;
    }

    public void setInstructors(Set<Long> instructors) {
        this.instructors = instructors;
    }
}
