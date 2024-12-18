package funix.sloc_system.entity;

import funix.sloc_system.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int sequence;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("sequence ASC")
    private List<Topic> topics;

    @Enumerated(EnumType.STRING)
    private CourseStatus status = CourseStatus.DRAFT;
}

