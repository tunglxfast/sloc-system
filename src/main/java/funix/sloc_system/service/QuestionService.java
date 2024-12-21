package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.entity.Question;
import funix.sloc_system.entity.Course;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.mapper.QuestionMapper;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.repository.QuestionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private ContentChangeService contentChangeService;
    
    @Autowired
    private ObjectMapper objectMapper;

    public List<Question> getQuestionsByTopic(Long topicId) {
        return questionRepository.findByTopicId(topicId);
    }

    /**
     * Get editing changes for question
     */
    @Transactional
    public QuestionDTO getEditingQuestionDTO(Long id) throws Exception {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        
        Optional<String> editingChanges = contentChangeService.getEntityEditing(EntityType.QUESTION, id)
                .map(change -> change.getChanges());
        
        if (editingChanges.isPresent()) {
            return objectMapper.readValue(editingChanges.get(), QuestionDTO.class);
        } else {
            return questionMapper.toDTO(question);
        }
    }

    /**
     * Save question changes based on course status
     */
    @Transactional
    public void saveQuestionChanges(QuestionDTO questionDTO, Long instructorId) throws IOException {
        Question question = questionRepository.findById(questionDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        
        Course course = question.getTopic().getChapter().getCourse();
        if (course.getContentStatus() == ContentStatus.DRAFT) {
            // Save directly to main table
            Question updatedQuestion = questionMapper.toEntity(questionDTO);
            updatedQuestion.setTopic(question.getTopic());
            // Set question reference for answers
            if (updatedQuestion.getAnswers() != null) {
                updatedQuestion.getAnswers().forEach(answer -> answer.setQuestion(updatedQuestion));
            }
            questionRepository.save(updatedQuestion);
        } else {
            // Save to temporary table
            contentChangeService.saveEditingQuestion(questionDTO, ContentAction.UPDATE, instructorId);
        }
    }
}

