package funix.sloc_system.entity;

import jakarta.persistence.*;

/**
 * Lớp cha của ReadingLesson, VideoLesson, Quiz, Exam
 */
@Entity
@Table(name = "topics")
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
    private TopicType type;

    // Thứ tự trong chapter
    @Column(nullable = false)
    private int order;

    @ManyToOne
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    public enum TopicType {
        READING, VIDEO, QUIZ, EXAM
    }

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

    public TopicType getType() {
        return type;
    }

    public void setType(TopicType type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }
}
