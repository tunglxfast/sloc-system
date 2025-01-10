package funix.sloc_system.entity;

import funix.sloc_system.enums.ApprovalStatus;
import funix.sloc_system.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @ManyToOne
    @JoinColumn(name = "instructor_id",nullable = false)
    private User instructor;
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "last_updated_by", nullable = true)
    private User lastUpdatedBy;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private ContentStatus contentStatus = ContentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.NOT_SUBMITTED;

    @Column(nullable = true)
    private String rejectReason;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate createdAt;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<Chapter> chapters = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Enrollment> enrollments;

    // Helper method

    // Update course with another course except chapters and enrollments
    public void updateWithOtherCourse(Course updatedCourse) {
        if (updatedCourse.getTitle() != null) {
            this.title = updatedCourse.getTitle();
        }
        if (updatedCourse.getDescription() != null) {
            this.description = updatedCourse.getDescription();
        }
        if (updatedCourse.getThumbnailUrl() != null) {
            this.thumbnailUrl = updatedCourse.getThumbnailUrl();
        }
        if (updatedCourse.getCategory() != null) {
            this.category = updatedCourse.getCategory();
        }
        if (updatedCourse.getInstructor() != null) {
            this.instructor = updatedCourse.getInstructor();
        }
        if (updatedCourse.getCreatedBy() != null) {
            this.createdBy = updatedCourse.getCreatedBy();
        }
        if (updatedCourse.getLastUpdatedBy() != null) {
            this.lastUpdatedBy = updatedCourse.getLastUpdatedBy();
        }
        if (updatedCourse.getStartDate() != null) {
            this.startDate = updatedCourse.getStartDate();
        }
        if (updatedCourse.getEndDate() != null) {
            this.endDate = updatedCourse.getEndDate();
        }
        if (updatedCourse.getContentStatus() != null) {
            this.contentStatus = updatedCourse.getContentStatus();
        }
        if (updatedCourse.getApprovalStatus() != null) {
            this.approvalStatus = updatedCourse.getApprovalStatus();
        }
        if (updatedCourse.getRejectReason() != null) {
            this.rejectReason = updatedCourse.getRejectReason();
        }
        if (updatedCourse.getCreatedAt() != null) {
            this.createdAt = updatedCourse.getCreatedAt();
        }
        if (updatedCourse.getUpdatedAt() != null) {
            this.updatedAt = updatedCourse.getUpdatedAt();
        }
    }

    // Helper methods
    public void addChapter(Chapter chapter) {
        if (chapters == null) {
            chapters = new ArrayList<>();
        }
        if (chapters.contains(chapter)) {
            return;
        }
        chapter.setCourse(this);
        chapters.add(chapter);
    }

    public void removeChapter(Chapter chapter) {
        if (chapters == null || !chapters.contains(chapter)) {
            return;
        }
        chapters.remove(chapter);
    }  

    public void setChaptersContentStatus(ContentStatus status) {
        for (Chapter chapter : chapters) {
            chapter.setContentStatus(status);
            chapter.setTopicsContentStatus(status);
        }
    }
}
