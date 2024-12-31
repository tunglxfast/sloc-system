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
    private Double highestScore;
    @Column
    private Double latestScore;
    @Column
    private Boolean passed;

    private Integer participationCount;
    @Column(nullable = false)
    private String testType; // Exam or Quiz
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    public TestResult(double highestScore, double latestScore, boolean passed, Integer participationCount, String testType, User user, Topic topic) {
        this.highestScore = highestScore;
        this.latestScore = latestScore;
        this.passed = passed;
        this.participationCount = participationCount;
        this.testType = testType;
        this.user = user;
        this.topic = topic;
    }

    public TestResult(double highestScore, double latestScore, boolean passed, Integer participationCount, TopicType testType, User user, Topic topic) {
        this.highestScore = highestScore;
        this.latestScore = latestScore;
        this.passed = passed;
        this.participationCount = participationCount;
        this.testType = testType.toString();
        this.user = user;
        this.topic = topic;
    }

    public void updateWithOtherTestResult(TestResult updatedTestResult) {
        if (updatedTestResult.getHighestScore() != null) {
            this.highestScore = updatedTestResult.getHighestScore();
        }
        if (updatedTestResult.getLatestScore() != null) {
            this.latestScore = updatedTestResult.getLatestScore();
        }
        if (updatedTestResult.getPassed() != null) {
            this.passed = updatedTestResult.getPassed();
        }
        if (updatedTestResult.getParticipationCount() != null) {
            this.participationCount = updatedTestResult.getParticipationCount();
        }
        if (updatedTestResult.getTestType() != null) {
            this.testType = updatedTestResult.getTestType();
        }
        if (updatedTestResult.getUser() != null) {
            this.user = updatedTestResult.getUser();
        }
        if (updatedTestResult.getTopic() != null) {
            this.topic = updatedTestResult.getTopic();
        }
    }
}
