package funix.sloc_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class StudyProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    @Column(name = "last_view_topic")
    private Long lastViewTopic;
    @Column(name = "learning_progress")
    private double learningProgress = 0.0;
    @Column(name = "final_score")
    private Integer finalScore;
    @Column(name = "pass_course")
    private Boolean passCourse;
    @Column(name = "progress_assessment")
    private String progressAssessment;
}
