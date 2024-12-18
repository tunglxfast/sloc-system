package funix.sloc_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {
    private Long id;
    private String content;
    private boolean isCorrect;
}
