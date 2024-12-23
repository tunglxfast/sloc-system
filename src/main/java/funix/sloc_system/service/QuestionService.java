package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.entity.Question;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.mapper.QuestionMapper;
import funix.sloc_system.repository.ContentChangeRepository;
import funix.sloc_system.repository.QuestionRepository;
import funix.sloc_system.util.ApplicationUtil;
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
    private ContentChangeRepository contentChangeRepository;
    @Autowired
    private ApplicationUtil appUtil;
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
        
        Optional<String> editingChanges = contentChangeRepository.findByEntityTypeAndEntityId(EntityType.QUESTION, id)
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
        
        if (question.getContentStatus() == ContentStatus.DRAFT) {
            int oldQuestionSequence = question.getSequence();
            // Update question using entity method
            Question updatedQuestion = questionMapper.toEntity(questionDTO);
            question.updateWithOtherQuestion(updatedQuestion);
            // Handle sequence change if needed
            if (oldQuestionSequence != questionDTO.getSequence()) {
                // Validate new sequence
                int maxSequence = questionRepository.findByTopicId(question.getTopic().getId()).size();
                int newSequence = Math.max(1, Math.min(questionDTO.getSequence(), maxSequence));
                
                // Reorder questions if sequence changed
                reorderQuestions(question.getTopic().getId(), question, newSequence);
            }
            questionRepository.save(question);
        } else {
            // Save question changes to temp table.
            // This includes both the question and its answers as a single unit.
            // The QuestionDTO should include the complete list of answers.
            appUtil.saveEntityChanges(EntityType.QUESTION, questionDTO, question.getId(), ContentAction.UPDATE, instructorId);
        }
    }

    /**
     * Reorder questions after sequence change
     */
    @Transactional
    private void reorderQuestions(Long topicId, Question modifiedQuestion, int newSequence) {
        int oldSequence = modifiedQuestion.getSequence();
        
        // Temporarily move question to a sequence outside the range (total questions + 1)
        int tempSequence = questionRepository.findByTopicId(topicId).size() + 1;
        modifiedQuestion.setSequence(tempSequence);
        questionRepository.save(modifiedQuestion);
        
        // Get all questions that need to be shifted
        List<Question> questions = questionRepository.findByTopicId(topicId);
        
        if (newSequence < oldSequence) {
            // Moving up: shift questions between new and old position down
            questions.stream()
                .filter(q -> q.getSequence() >= newSequence && q.getSequence() < oldSequence)
                .forEach(q -> {
                    q.setSequence(q.getSequence() + 1);
                    questionRepository.save(q);
                });
        } else {
            // Moving down: shift questions between old and new position up
            questions.stream()
                .filter(q -> q.getSequence() > oldSequence && q.getSequence() <= newSequence)
                .forEach(q -> {
                    q.setSequence(q.getSequence() - 1);
                    questionRepository.save(q);
                });
        }
        
        // Set the modified question to its new sequence
        modifiedQuestion.setSequence(newSequence);
        questionRepository.save(modifiedQuestion);
    }
}

