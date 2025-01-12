package funix.sloc_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    private Long chapterId;
    private String contentStatus;
    
    // Fields for ReadingLesson
    private String fileUrl;
    
    // Fields for VideoLesson
    private String videoUrl;
    
    // Fields for Quiz and Exam
    private Integer passPoint;
    private Integer maxPoint;
    private Integer timeLimit; // Only for Exam
    private List<QuestionDTO> questions;

    // Helper methods
    public void addQuestion(QuestionDTO questionDTO) {
        if (questions == null) {
            questions = new ArrayList<>();
        }
        if (questions.contains(questionDTO)) {
            return;
        }
        questionDTO.setTopicId(id);
        questions.add(questionDTO);
    }
}
