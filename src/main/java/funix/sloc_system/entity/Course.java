package funix.sloc_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import funix.sloc_system.enums.CourseStatus;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    // course image
    private String thumbnailUrl;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "last_updated_by", nullable = true)
    private User lastUpdatedBy;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("sequence ASC")
    @JsonIgnore
    private List<Chapter> chapters;

    @OneToMany(mappedBy = "course", cascade = CascadeType.PERSIST)
    @JsonIgnore
    private Set<Enrollment> enrollments = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "instructor_course",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> instructors = new HashSet<>();

    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(nullable = true)
    private String rejectReason;

    private LocalDate createdAt;
    private LocalDate updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDate.now();
    }

    // getter, setter
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public Set<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(Set<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public Set<User> getInstructors() {
        return instructors;
    }

    public void setInstructors(Set<User> instructors) {
        this.instructors = instructors;
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

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
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

    @JsonProperty("lastUpdatedBy")
    public Long getUserIdThatLastUpdateCourse() {
        return lastUpdatedBy != null ? lastUpdatedBy.getId() : null;
    }

    @JsonProperty("createdBy")
    public Long getUserIdThatCreateCourse() {
        return createdBy != null ? createdBy.getId() : null;
    }

    @JsonProperty("category")
    public Long getIdOfCourseCategory() {
        return category != null ? category.getId() : null;
    }

    // helping hand methods
    public void updateCourse(Course otherCourse) {
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
        if (otherCourse.getEnrollments() != null) {
            this.setEnrollments(otherCourse.getEnrollments());
        }
        if (otherCourse.getInstructors() != null) {
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
        if (otherCourse.getCreatedAt() != null) {
            this.setCreatedAt(otherCourse.getCreatedAt());
        }
        if (otherCourse.getUpdatedAt() != null) {
            this.setUpdatedAt(otherCourse.getUpdatedAt());
        }
    }
}
