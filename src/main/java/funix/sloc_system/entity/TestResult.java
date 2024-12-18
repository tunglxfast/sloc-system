package funix.sloc_system.entity;

import funix.sloc_system.enums.TopicType;
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
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Integer highestScore;
    @Column
    private Integer latestScore;
    @Column
    private Boolean passed;

    private Integer participationCount;
    @Column(nullable = false)
    private String testType;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    public TestResult(int highestScore, int latestScore, boolean passed, Integer participationCount, String testType, User user, Topic topic) {
        this.highestScore = highestScore;
        this.latestScore = latestScore;
        this.passed = passed;
        this.participationCount = participationCount;
        this.testType = testType;
        this.user = user;
        this.topic = topic;
    }

    public TestResult(int highestScore, int latestScore, boolean passed, Integer participationCount, TopicType testType, User user, Topic topic) {
        this.highestScore = highestScore;
        this.latestScore = latestScore;
        this.passed = passed;
        this.participationCount = participationCount;
        this.testType = testType.toString();
        this.user = user;
        this.topic = topic;
    }
}
