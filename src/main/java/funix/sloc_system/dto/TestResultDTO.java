package funix.sloc_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestResultDTO {
    private Long id;
    private Integer highestScore;
    private Integer latestScore;
    private Boolean passed;
    private Integer participationCount;
    private String testType;
    private UserDTO user;
    private TopicDTO topic;
}
