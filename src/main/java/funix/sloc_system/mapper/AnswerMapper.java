package funix.sloc_system.mapper;

import funix.sloc_system.dto.AnswerDTO;
import funix.sloc_system.entity.Answer;
import funix.sloc_system.entity.Question;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.repository.QuestionRepository;
import funix.sloc_system.repository.AnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnswerMapper {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;
    
    public AnswerDTO toDTO(Answer answer) {
        if (answer == null) {
            return null;
        }

        AnswerDTO dto = new AnswerDTO();
        dto.setId(answer.getId());
        dto.setContent(answer.getContent());
        dto.setContentStatus(answer.getContentStatus().name());

        if (answer.getQuestion() != null) { 
            dto.setQuestionId(answer.getQuestion().getId());
        }

        dto.setCorrect(answer.isCorrect());
        
        return dto;
    }

    public Answer toEntity(AnswerDTO dto, Question question) {
        if (dto == null) {
            return null;
        }

        Answer answer;
        if (dto.getId() != null) {
            answer = answerRepository.findById(dto.getId()).orElse(new Answer());
        } else {
            answer = new Answer();
        }

        if (answer.getId() == null && dto.getId() != null) {
            answer.setId(dto.getId());
        }

        answer.setContent(dto.getContent());
        if (dto.getContentStatus() != null) {
            answer.setContentStatus(ContentStatus.valueOf(dto.getContentStatus()));
        }
        answer.setQuestion(question);
        answer.setCorrect(dto.isCorrect());
        
        return answer;
    }

    public List<AnswerDTO> toDTO(List<Answer> answers) {
        return answers.stream().map(answer -> toDTO(answer)).collect(Collectors.toList());
    }

    public List<Answer> toEntity(List<AnswerDTO> answerDTOList, Question question) {
        return answerDTOList.stream().map(dto -> toEntity(dto, question)).collect(Collectors.toList());
    }
} 