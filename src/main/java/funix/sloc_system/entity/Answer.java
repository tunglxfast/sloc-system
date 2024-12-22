package funix.sloc_system.entity;

import funix.sloc_system.dto.AnswerDTO;
import funix.sloc_system.enums.ContentStatus;
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
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private boolean isCorrect;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    private ContentStatus contentStatus = ContentStatus.DRAFT;

    public void updateWithOtherAnswer(Answer updatedAnswer) {
        if (updatedAnswer.getContent() != null) {
            this.content = updatedAnswer.getContent();
        }
        if (updatedAnswer.getQuestion() != null) {
            this.question = updatedAnswer.getQuestion();
        }
        if (updatedAnswer.getContentStatus() != null) {
            this.contentStatus = updatedAnswer.getContentStatus();
        }
        this.isCorrect = updatedAnswer.isCorrect();
    }
}
