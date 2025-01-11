package funix.sloc_system.entity;

import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.TopicType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
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
    private Integer passPoint = 0;
    private Integer maxPoint = 0;
    private Integer timeLimit; // Only for Exam

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TopicDiscussion> discussions = new ArrayList<>();

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
        if (updatedTopic.getVideoUrl() != null) {
            this.videoUrl = updatedTopic.getVideoUrl();
        }
        if (updatedTopic.getFileUrl() != null) {
            this.fileUrl = updatedTopic.getFileUrl();
        }
        if (updatedTopic.getPassPoint() != null) {
            this.passPoint = updatedTopic.getPassPoint();
        }
        if (updatedTopic.getMaxPoint() != null) {
            this.maxPoint = updatedTopic.getMaxPoint();
        }
        if (updatedTopic.getTimeLimit() != null) {
            this.timeLimit = updatedTopic.getTimeLimit();
        }
        if (updatedTopic.getContentStatus() != null) {
            this.contentStatus = updatedTopic.getContentStatus();
        }
    }

    // Helper method to add question
    public void addQuestion(Question question) {
        if (questions == null) {
            questions = new ArrayList<>();
        }
        if (questions.contains(question)) {
            return;
        }
        questions.add(question);
        question.setTopic(this);
    }

    // Helper method to remove question
    public void removeQuestion(Question question) {
        if (questions == null || !questions.contains(question)) {
            return;
        }
        questions.remove(question);
        question.setTopic(null);
    }

    public void setQuestionsContentStatus(ContentStatus status) {
        for (Question question : questions) {
            question.setContentStatus(status);
            question.setAnswersContentStatus(status);
        }
    }

    // Helper method to add discussion
    public void addDiscussion(TopicDiscussion discussion) {
        if (discussions == null) {
            discussions = new ArrayList<>();
        }
        discussions.add(discussion);
        discussion.setTopic(this);
    }

    // Helper method to remove discussion
    public void removeDiscussion(TopicDiscussion discussion) {
        if (discussions != null) {
            discussions.remove(discussion);
            discussion.setTopic(null);
        }
    }
}

