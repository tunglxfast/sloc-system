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
public class TopicDTO {
    private Long id;
    private String title;
    private String topicType;
    private int sequence;
    private List<QuestionDTO> questions;
}
