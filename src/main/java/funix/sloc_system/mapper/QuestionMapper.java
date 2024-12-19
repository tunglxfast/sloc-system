package funix.sloc_system.mapper;

import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.entity.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class QuestionMapper {
    
    @Autowired
    private AnswerMapper answerMapper;

    public QuestionDTO toDTO(Question question) {
        if (question == null) {
            return null;
        }

        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setContent(question.getContent());
        dto.setQuestionType(question.getQuestionType());
        dto.setSequence(question.getSequence());
        
        // Map answers
        dto.setAnswers(question.getAnswers().stream()
                .map(answerMapper::toDTO)
                .toList());
        
        return dto;
    }

    public Question toEntity(QuestionDTO dto) {
        if (dto == null) {
            return null;
        }

        Question question = new Question();
        question.setId(dto.getId());
        question.setContent(dto.getContent());
        question.setQuestionType(dto.getQuestionType());
        question.setSequence(dto.getSequence());
        
        // Map answers if present
        if (dto.getAnswers() != null) {
            question.setAnswers(dto.getAnswers().stream()
                    .map(answerMapper::toEntity)
                    .toList());
        } else {
            question.setAnswers(new ArrayList<>());
        }
        
        return question;
    }
} 