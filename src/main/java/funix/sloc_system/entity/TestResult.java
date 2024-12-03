package funix.sloc_system.entity;

import funix.sloc_system.enums.TopicType;
import jakarta.persistence.*;

@Entity
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int highestScore;
    @Column(nullable = false)
    private int latestScore;
    @Column(nullable = false)
    private boolean passed;

    private Integer participationCount;
    @Column(nullable = false)
    private String testType;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    public TestResult() {
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public int getLatestScore() {
        return latestScore;
    }

    public void setLatestScore(int latestScore) {
        this.latestScore = latestScore;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Integer getParticipationCount() {
        return participationCount;
    }

    public void setParticipationCount(Integer participationCount) {
        this.participationCount = participationCount;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }


}
