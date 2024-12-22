package funix.sloc_system.mapper;

import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.entity.Question;
import funix.sloc_system.enums.QuestionType;
import funix.sloc_system.repository.TopicRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class QuestionMapper {
    
    @Autowired
    private AnswerMapper answerMapper;
    @Autowired
    private TopicRepository topicRepository;

    public QuestionDTO toDTO(Question question) {
        if (question == null) {
            return null;
        }

        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setContent(question.getContent());
        dto.setQuestionType(question.getQuestionType().toString());
        dto.setSequence(question.getSequence());
        dto.setTopicId(question.getTopic() != null ? question.getTopic().getId() : null);
        
        // Map answers
        dto.setAnswers(answerMapper.toDTO(question.getAnswers()));
        
        return dto;
    }

    public Question toEntity(QuestionDTO dto) {
        if (dto == null) {
            return null;
        }

        Question question = new Question();
        question.setId(dto.getId());
        question.setContent(dto.getContent());
        if (dto.getQuestionType() != null) {
            question.setQuestionType(QuestionType.valueOf(dto.getQuestionType()));
        }
        question.setSequence(dto.getSequence());
        question.setTopic(topicRepository.findById(dto.getTopicId()).orElse(null));

        // Map answers if present
        if (dto.getAnswers() != null) {
            question.setAnswers(answerMapper.toEntity(dto.getAnswers()));
        } else {
            question.setAnswers(new ArrayList<>());
        }
        
        return question;
    }

    public List<QuestionDTO> toDTO(List<Question> questions) {
        return questions.stream().map(this::toDTO).toList();
    }

    public List<Question> toEntity(List<QuestionDTO> questionDTOList) {
        return questionDTOList.stream().map(this::toEntity).toList();
    }
} 