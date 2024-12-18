package funix.sloc_system.entity;

import funix.sloc_system.enums.CourseStatus;
import funix.sloc_system.enums.TopicType;
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
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopicType topicType;

    // topic order
    @Column(nullable = false)
    private int sequence;

    @ManyToOne
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Enumerated(EnumType.STRING)
    private CourseStatus status = CourseStatus.DRAFT;

    // Fields for ReadingLesson
    private String fileUrl;

    // Fields for VideoLesson
    private String videoUrl;

    // Fields for Quiz and Exam
    private Integer passScore;
    private Integer totalScore;
    private Integer timeLimit; // Only for Exam

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Question> questions;
}
