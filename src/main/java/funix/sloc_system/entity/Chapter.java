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

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("sequence ASC")
    private List<Topic> topics = new ArrayList<>();

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
        if (updatedChapter.getTopics() != null) {
            this.topics = updatedChapter.getTopics();
        }
        if (updatedChapter.getContentStatus() != null) {
            this.contentStatus = updatedChapter.getContentStatus();
        }
    }
}
