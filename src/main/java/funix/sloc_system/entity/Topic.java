package funix.sloc_system.entity;

import funix.sloc_system.enums.ContentStatus;
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
    private ContentStatus contentStatus = ContentStatus.DRAFT;

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


    public void updateWithOtherTopic(Topic updatedTopic) {
        if (updatedTopic.getTitle() != null) {
            this.title = updatedTopic.getTitle();
        }
        if (updatedTopic.getDescription() != null) {
            this.description = updatedTopic.getDescription();
        }
        if (updatedTopic.getTopicType() != null) {
            this.topicType = updatedTopic.getTopicType();
        }
        if (updatedTopic.getSequence() != 0) {
            this.sequence = updatedTopic.getSequence();
        }
        if (updatedTopic.getChapter() != null) {
            this.chapter = updatedTopic.getChapter();
        }
        if (updatedTopic.getContentStatus() != null) {
            this.contentStatus = updatedTopic.getContentStatus();
        }
        if (updatedTopic.getFileUrl() != null) {
            this.fileUrl = updatedTopic.getFileUrl();
        }
        if (updatedTopic.getVideoUrl() != null) {
            this.videoUrl = updatedTopic.getVideoUrl();
        }
        if (updatedTopic.getPassScore() != null) {
            this.passScore = updatedTopic.getPassScore();
        }
        if (updatedTopic.getTotalScore() != null) {
            this.totalScore = updatedTopic.getTotalScore();
        }
        if (updatedTopic.getTimeLimit() != null) {
            this.timeLimit = updatedTopic.getTimeLimit();
        }
        if (updatedTopic.getQuestions() != null) {
            this.questions = updatedTopic.getQuestions();
        }
    }
        
}
