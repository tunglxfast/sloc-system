package funix.sloc_system.dto;

import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.TopicType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopicDTO {
    private Long id;
    private String title;
    private String description;
    private String topicType;
    private int sequence;
    @JsonIgnore
    private Long chapterId;
    private String contentStatus;
    
    // Fields for ReadingLesson
    private String fileUrl;
    
    // Fields for VideoLesson
    private String videoUrl;
    
    // Fields for Quiz and Exam
    private Integer passScore;
    private Integer totalScore;
    private Integer timeLimit; // Only for Exam
    private List<QuestionDTO> questions;
}
