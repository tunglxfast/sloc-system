package funix.sloc_system.mapper;

import funix.sloc_system.dto.AnswerDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.entity.Answer;
import funix.sloc_system.entity.Question;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.QuestionType;
import funix.sloc_system.repository.QuestionRepository;
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
    @Autowired
    private QuestionRepository questionRepository;

    public QuestionDTO toDTO(Question question) {
        if (question == null) {
            return null;
        }

        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setContent(question.getContent());
        dto.setContentStatus(question.getContentStatus().name());
        dto.setQuestionType(question.getQuestionType().name());

        if (question.getTopic() != null) {
            dto.setTopicId(question.getTopic().getId());
        }
        
        if (question.getAnswers() != null) {
            for (Answer answer : question.getAnswers()) {
                dto.addAnswer(answerMapper.toDTO(answer));
            }
        }

        return dto;
    }

    public List<QuestionDTO> toDTO(List<Question> questions) {
        if (questions == null) {
            return new ArrayList<>();
        }
        return questions.stream()
                .map(question -> toDTO(question))
                .collect(Collectors.toList());
    }

    public Question toEntity(QuestionDTO dto, Topic topic) {
        if (dto == null) {
            return null;
        }

        Question question;
        if (dto.getId() != null) {
            question = questionRepository.findById(dto.getId()).orElse(new Question());
        } else {
            question = new Question();
        }

        if (question.getId() == null && dto.getId() != null) {
            question.setId(dto.getId());
        }

        question.setContent(dto.getContent());
        question.setContentStatus(ContentStatus.valueOf(dto.getContentStatus()));
        question.setQuestionType(QuestionType.valueOf(dto.getQuestionType()));

        // Add Topic setting
        question.setTopic(topic);

        
        if (dto.getAnswers() != null) {
            if (question.getAnswers() != null) {
                question.getAnswers().clear();
            } else {
                question.setAnswers(new ArrayList<>());
            }
            for (AnswerDTO answerDTO : dto.getAnswers()) {
                question.addAnswer(answerMapper.toEntity(answerDTO, question));
            }
        }

        return question;
    }

    public List<Question> toEntity(List<QuestionDTO> dtos, Topic topic) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(dto -> toEntity(dto, topic))
                .collect(Collectors.toList());
    }
} 