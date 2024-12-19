package funix.sloc_system.entity;

import funix.sloc_system.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
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
    private CourseStatus status = CourseStatus.DRAFT;
    @Column(nullable = true)
    private String rejectReason;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate createdAt;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("sequence ASC")
    private List<Chapter> chapters;

    @OneToMany(mappedBy = "course", cascade = CascadeType.PERSIST)
    private Set<Enrollment> enrollments = new HashSet<>();

}
