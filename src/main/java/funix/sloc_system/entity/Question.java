package funix.sloc_system.entity;

import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.AnswerDTO;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.QuestionType;
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
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType; // CHOICE_MANY, CHOICE_ONE

    private int sequence;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Answer> answers;

    @Enumerated(EnumType.STRING)
    private ContentStatus contentStatus = ContentStatus.DRAFT;

    public void updateWithOtherQuestion(Question updatedQuestion) {
        if (updatedQuestion.getContent() != null) {
            this.content = updatedQuestion.getContent();
        }
        if (updatedQuestion.getQuestionType() != null) {
            this.questionType = updatedQuestion.getQuestionType();
        }
        if (updatedQuestion.getSequence() != 0) {
            this.sequence = updatedQuestion.getSequence();
        }
        if (updatedQuestion.getTopic() != null) {
            this.topic = updatedQuestion.getTopic();
        }
        if (updatedQuestion.getAnswers() != null) {
            this.answers = updatedQuestion.getAnswers();
        }
        if (updatedQuestion.getContentStatus() != null) {
            this.contentStatus = updatedQuestion.getContentStatus();
        }
    }
}
