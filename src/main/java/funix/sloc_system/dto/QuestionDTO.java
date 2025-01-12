package funix.sloc_system.dto;

import funix.sloc_system.enums.QuestionType;
import funix.sloc_system.enums.ContentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private String content;
    private String contentStatus;
    private String questionType; // CHOICE_MANY, CHOICE_ONE, INPUT_TEXT
    private Integer point = 0;
    private Long topicId;  // Parent topic ID
    private List<AnswerDTO> answers;

    // Helper methods
    public void addAnswer(AnswerDTO answerDTO) {
        if (answers == null) {
            answers = new ArrayList<>();
        }
        if (answers.contains(answerDTO)) {
            return;
        }
        answerDTO.setQuestionId(id);
        answers.add(answerDTO);
    }
}
