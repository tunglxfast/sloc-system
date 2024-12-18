package funix.sloc_system.dto;

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
    private String questionType; // MULTIPLE_CHOICE, TRUE_FALSE
    private int sequence;
    private List<AnswerDTO> answers;
}
