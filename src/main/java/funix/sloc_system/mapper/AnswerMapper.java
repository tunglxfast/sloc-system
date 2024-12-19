package funix.sloc_system.mapper;

import funix.sloc_system.dto.AnswerDTO;
import funix.sloc_system.entity.Answer;
import org.springframework.stereotype.Component;

@Component
public class AnswerMapper {
    
    public AnswerDTO toDTO(Answer answer) {
        if (answer == null) {
            return null;
        }

        AnswerDTO dto = new AnswerDTO();
        dto.setId(answer.getId());
        dto.setContent(answer.getContent());
        dto.setCorrect(answer.isCorrect());
        
        return dto;
    }

    public Answer toEntity(AnswerDTO dto) {
        if (dto == null) {
            return null;
        }

        Answer answer = new Answer();
        answer.setId(dto.getId());
        answer.setContent(dto.getContent());
        answer.setCorrect(dto.isCorrect());
        
        return answer;
    }
} 