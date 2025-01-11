package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Question;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.mapper.ChapterMapper;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.mapper.QuestionMapper;
import funix.sloc_system.mapper.TopicMapper;
import funix.sloc_system.repository.ContentChangeRepository;
import funix.sloc_system.repository.QuestionRepository;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.util.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private TopicRepository topicRepository;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private ContentChangeRepository contentChangeRepository;

    @Autowired
    private AppUtil appUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private ChapterMapper chapterMapper;

    public List<Question> getQuestionsByTopic(Long topicId) {
        return questionRepository.findByTopicId(topicId);
    }

    /**
     * Save question changes based on course status
     */
    @Transactional
    public void saveQuestionChanges(QuestionDTO questionDTO, Long instructorId) throws Exception {
        Question question = questionRepository.findById(questionDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        
        Topic topic = question.getTopic();
        if (topic == null) {
            throw new IllegalStateException("Question has no associated topic");
        }
        
        Course course = topic.getChapter().getCourse();
        
        if (course.getContentStatus() == ContentStatus.DRAFT) {
            Question updatedQuestion = questionMapper.toEntity(questionDTO, topic);
            question.updateWithOtherQuestion(updatedQuestion);
            questionRepository.save(question);
        }    
        else {
            // Get latest course DTO with any pending changes
            CourseDTO courseDTO = appUtil.getEditingCourseDTO(course.getId());
            
            // Update the question in courseDTO
            ChapterDTO chapterDTO = AppUtil.getSelectChapterDTO(courseDTO, topic.getChapter().getId());
            TopicDTO topicDTO = AppUtil.getSelectTopicDTO(chapterDTO, topic.getId());
            
            // Find and update the question in the topic's questions list
            for (QuestionDTO existingQuestionDTO : topicDTO.getQuestions()) {
                if (existingQuestionDTO.getId() == null) {
                    continue;
                }
                if (existingQuestionDTO.getId().equals(questionDTO.getId())) {
                    existingQuestionDTO.setContent(questionDTO.getContent());
                    existingQuestionDTO.setQuestionType(questionDTO.getQuestionType());
                    existingQuestionDTO.setAnswers(questionDTO.getAnswers());
                    break;
                }
            }
            
            // Save entire course DTO to temp table
            String json = objectMapper.writeValueAsString(courseDTO);
            appUtil.saveContentChange(json, course.getId(), instructorId, ContentAction.UPDATE);
        }
    }

    /**
     * Handle batch update of questions for a topic
     */
    @Transactional
    public List<QuestionDTO> handleTopicQuestions(Long topicId, List<QuestionDTO> questionDTOs, Long instructorId) throws Exception {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        List<QuestionDTO> newQuestionDTOs = new ArrayList<>();
        Course course = topic.getChapter().getCourse();
        
        if (course.getContentStatus() == ContentStatus.DRAFT) {
            // For draft courses, handle each question directly in the database
            List<Question> existingQuestions = getQuestionsByTopic(topicId);
            List<Long> updatedQuestionIds = questionDTOs.stream()
                .map(QuestionDTO::getId)
                .filter(id -> id != null)
                .toList();

            // Delete questions not in the new list
            for (Question existingQuestion : existingQuestions) {
                if (!updatedQuestionIds.contains(existingQuestion.getId())) {
                    questionRepository.delete(existingQuestion);
                }
            }

            // Update or create questions
            QuestionDTO newQuestionDTO;
            for (QuestionDTO questionDTO : questionDTOs) {
                questionDTO.setTopicId(topicId);
                if (questionDTO.getId() == null) {
                    newQuestionDTO = createQuestion(topicId, questionDTO, instructorId);
                    if (newQuestionDTO != null) {
                        newQuestionDTOs.add(newQuestionDTO);
                    }
                } else {
                    saveQuestionChanges(questionDTO, instructorId);
                    newQuestionDTOs.add(questionDTO);
                }
            }
        }
        else {
            // Get latest course DTO with any pending changes
            CourseDTO courseDTO = appUtil.getEditingCourseDTO(course.getId());
            QuestionDTO newQuestionDTO;
            // Always save new questions to main table first
            for (QuestionDTO questionDTO : questionDTOs) {
                if (questionDTO.getId() == null) {
                    newQuestionDTO = createQuestion(topicId, questionDTO, instructorId);
                    if (newQuestionDTO != null) {
                        newQuestionDTOs.add(newQuestionDTO);
                    }
                } else {
                    newQuestionDTOs.add(questionDTO);
                }
            }

            // Update course DTO with the complete list of questions
            ChapterDTO chapterDTO = AppUtil.getSelectChapterDTO(courseDTO, topic.getChapter().getId());
            TopicDTO topicDTO = AppUtil.getSelectTopicDTO(chapterDTO, topicId);
            topicDTO.setQuestions(newQuestionDTOs);
            
            // Save to temp table
            String json = objectMapper.writeValueAsString(courseDTO);
            appUtil.saveContentChange(json, course.getId(), instructorId, ContentAction.UPDATE);
        }

        return newQuestionDTOs;
    }

    /**
     * Create a new question
     */
    @Transactional
    public QuestionDTO createQuestion(Long topicId, QuestionDTO questionDTO, Long instructorId) throws Exception {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        
        Course course = topic.getChapter().getCourse();
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(course.getId());
        
        // Always save new question to main table first
        Question newQuestion = questionMapper.toEntity(questionDTO, topic);
        newQuestion.setTopic(topic);
        questionRepository.save(newQuestion);
        
        topic.getQuestions().add(newQuestion);
        topic.setMaxPoint(topic.getMaxPoint() + questionDTO.getPoint());
        topicRepository.save(topic);
        
        QuestionDTO newQuestionDTO = questionMapper.toDTO(newQuestion);
        // If course is not draft, save entire course DTO to temp table
        if (course.getContentStatus() != ContentStatus.DRAFT) {
            // Add new question to course DTO
            ChapterDTO chapterDTO = AppUtil.getSelectChapterDTO(courseDTO, topic.getChapter().getId());
            TopicDTO topicDTO = AppUtil.getSelectTopicDTO(chapterDTO, topicId);
            topicDTO.getQuestions().add(newQuestionDTO);
            
            // Save to temp table
            String json = objectMapper.writeValueAsString(courseDTO);
            appUtil.saveContentChange(json, course.getId(), instructorId, ContentAction.UPDATE);
        }

        return newQuestionDTO;
    }
}

