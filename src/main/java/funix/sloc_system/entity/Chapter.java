package funix.sloc_system.entity;

import funix.sloc_system.enums.ContentStatus;
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
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int sequence;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Topic> topics;

    @Enumerated(EnumType.STRING)
    private ContentStatus contentStatus = ContentStatus.DRAFT;

    public void updateWithOtherChapter(Chapter updatedChapter) {
        if (updatedChapter.getTitle() != null) {
            this.title = updatedChapter.getTitle();
        }
        if (updatedChapter.getSequence() != 0) {
            this.sequence = updatedChapter.getSequence();
        }
        if (updatedChapter.getCourse() != null) {
            this.course = updatedChapter.getCourse();
        }
        if (updatedChapter.getContentStatus() != null) {
            this.contentStatus = updatedChapter.getContentStatus();
        }
    }

    // Helper method to add topic
    public void addTopic(Topic topic) {
        if (topics == null) {
            topics = new ArrayList<>();
        }
        if (topics.contains(topic)) {
            return;
        }
        topics.add(topic);
        topic.setChapter(this);
    }

    // Helper method to remove topic
    public void removeTopic(Topic topic) {
        if (topics == null || !topics.contains(topic)) {
            return;
        }
        topics.remove(topic);
        topic.setChapter(null);
    }
}
