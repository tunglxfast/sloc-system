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

import java.util.ArrayList;
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

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ContentStatus contentStatus = ContentStatus.DRAFT;

    public void updateWithOtherQuestion(Question updatedQuestion) {
        if (updatedQuestion.getContent() != null) {
            this.content = updatedQuestion.getContent();
        }
        if (updatedQuestion.getQuestionType() != null) {
            this.questionType = updatedQuestion.getQuestionType();
        }
        if (updatedQuestion.getTopic() != null) {
            this.topic = updatedQuestion.getTopic();
        }
        if (updatedQuestion.getContentStatus() != null) {
            this.contentStatus = updatedQuestion.getContentStatus();
        }
    }

    // Helper method to add answer
    public void addAnswer(Answer answer) {
        if (answers == null) {
            answers = new ArrayList<>();
        }
        if (answers.contains(answer)) {
            return;
        }
        answers.add(answer);
        answer.setQuestion(this);
    }

    // Helper method to remove answer
    public void removeAnswer(Answer answer) {
        if (answers == null || !answers.contains(answer)) {
            return;
        }
        answers.remove(answer);
        answer.setQuestion(null);
    }

    public void setAnswersContentStatus(ContentStatus status) {
        for (Answer answer : answers) {
            answer.setContentStatus(status);
        }
    }
}
