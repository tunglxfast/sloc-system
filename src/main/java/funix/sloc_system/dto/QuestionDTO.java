package funix.sloc_system.dto;

import funix.sloc_system.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private String content;
    
    private String questionType; // CHOICE_MANY, CHOICE_ONE
    private Long topicId;  // Parent topic ID
    private List<AnswerDTO> answers;
}
