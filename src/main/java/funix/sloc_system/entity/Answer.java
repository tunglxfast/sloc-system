package funix.sloc_system.entity;

import funix.sloc_system.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private boolean isCorrect;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    private CourseStatus status = CourseStatus.DRAFT;
}
