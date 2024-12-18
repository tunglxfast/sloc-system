package funix.sloc_system.entity;

import funix.sloc_system.enums.CourseChangeAction;
import funix.sloc_system.enums.CourseStatus;
import funix.sloc_system.enums.EntityType;
import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "course_change_temporary")
public class CourseChangeTemporary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityType entityType;
    @Column(nullable = false)
    private Long entityId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseChangeAction action;
    @Column(length = 5000, nullable = false)
    private String changes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status = CourseStatus.PENDING_EDIT; // Only have PENDING_EDIT and REJECTED

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User updatedBy;

    @Column(nullable = false)
    private LocalDateTime changeTime;
}
