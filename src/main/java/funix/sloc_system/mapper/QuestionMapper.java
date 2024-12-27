package funix.sloc_system.mapper;

import funix.sloc_system.dto.AnswerDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.entity.Answer;
import funix.sloc_system.entity.Question;
import funix.sloc_system.enums.QuestionType;
import funix.sloc_system.repository.TopicRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        dto.setQuestionType(question.getQuestionType().name());

        if (question.getTopic() != null) {
            dto.setTopicId(question.getTopic().getId());
        }
        
        if (question.getAnswers() != null) {
            dto.setAnswers(answerMapper.toDTO(question.getAnswers()));
        }

        return dto;
    }

    public List<QuestionDTO> toDTO(List<Question> questions) {
        if (questions == null) {
            return new ArrayList<>();
        }
        return questions.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Question toEntity(QuestionDTO dto) {
        if (dto == null) {
            return null;
        }

        Question question = new Question();
        question.setId(dto.getId());
        question.setContent(dto.getContent());
        question.setQuestionType(QuestionType.valueOf(dto.getQuestionType()));

        // Add Topic setting
        if (dto.getTopicId() != null) {
            question.setTopic(topicRepository.findById(dto.getTopicId()).orElse(null));
        }

        
        if (dto.getAnswers() != null) {
            List<Answer> answers = answerMapper.toEntity(dto.getAnswers());
            answers.forEach(answer -> answer.setQuestion(question));
            question.setAnswers(answers);
        }

        return question;
    }

    public List<Question> toEntity(List<QuestionDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
} 