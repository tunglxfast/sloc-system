package funix.sloc_system.entity;

import funix.sloc_system.enums.TopicType;
import jakarta.persistence.*;

/**
 * Lớp cha của ReadingLesson, VideoLesson, Quiz, Exam
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopicType topicType;

    // Thứ tự trong chapter
    @Column(nullable = false)
    private int sequence;

    @ManyToOne
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TopicType getTopicType() {
        return topicType;
    }

    public void setTopicType(TopicType topicType) {
        this.topicType = topicType;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }
}
