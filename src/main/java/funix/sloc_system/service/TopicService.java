package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Question;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.mapper.TopicMapper;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.ContentChangeRepository;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.util.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TopicService {
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private ChapterRepository chapterRepository;    
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ContentChangeRepository contentChangeRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AppUtil appUtil;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TopicMapper topicMapper;

    public Topic findById(Long id) {
        return topicRepository.findById(id).orElse(null);
    }

    public List<Topic> findByChapterId(Long chapterId) {
        return topicRepository.findByChapterId(chapterId);
    }

    public Topic findByChapterAndTopicSequence(Long courseId, int chapterSequence, int topicSequence) {
        Chapter chapter = chapterRepository.findByCourseIdAndSequence(courseId, chapterSequence).orElse(null);
        if (chapter == null) {
            return null;
        }

        Optional<Topic> topic = topicRepository.findByChapterIdAndSequence(chapter.getId(), topicSequence);
        if (topic.isEmpty()) {
            return null;
        } else {
            return topic.get();
        }
    }

    /**
     * Get editing changes for topic
     */
    @Transactional
    public TopicDTO getEditingTopicDTO(Long id) throws Exception {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        
        Optional<String> editingChanges = contentChangeRepository.findByEntityTypeAndEntityId(EntityType.TOPIC, id)
                .map(change -> change.getChanges());
        
        if (editingChanges.isPresent()) {
            return objectMapper.readValue(editingChanges.get(), TopicDTO.class);
        } else {
            return topicMapper.toDTO(topic);
        }
    }

    /**
     * Save topic changes based on course status
     */
    @Transactional
    public void saveTopicChanges(Long courseId, Long chapterId, TopicDTO topicDTO, Long instructorId) throws Exception {
        Long topicId = topicDTO.getId();

        // Get latest course DTO with any pending changes
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);
                    
        // Update the topic in courseDTO
        ChapterDTO chapterDTO = AppUtil.getSelectChapterDTO(courseDTO, chapterId);
        TopicDTO existingTopicDTO = AppUtil.getSelectTopicDTO(chapterDTO, topicId);
        // Copy all properties from topicDTO to existingTopicDTO
        if (topicDTO.getTitle() != null) {
            existingTopicDTO.setTitle(topicDTO.getTitle());
        }
        if (topicDTO.getDescription() != null) {
            existingTopicDTO.setDescription(topicDTO.getDescription());
        }
        if (topicDTO.getSequence() != 0) {
            existingTopicDTO.setSequence(topicDTO.getSequence());
        }
        if (topicDTO.getFileUrl() != null) {
            existingTopicDTO.setFileUrl(topicDTO.getFileUrl());
        }
        if (topicDTO.getVideoUrl() != null) {
            existingTopicDTO.setVideoUrl(topicDTO.getVideoUrl());
        }
        if (topicDTO.getQuestions() != null) {
            existingTopicDTO.getQuestions().clear();
            for (QuestionDTO questionDTO : topicDTO.getQuestions()) {
                existingTopicDTO.addQuestion(questionDTO);
            }
        }
        if (topicDTO.getPassScore() != null) {
            existingTopicDTO.setPassScore(topicDTO.getPassScore());
        }
        if (topicDTO.getTimeLimit() != null) {
            existingTopicDTO.setTimeLimit(topicDTO.getTimeLimit());
        }
        
        if (courseDTO.getContentStatus().equals(ContentStatus.DRAFT.name())) {
            Chapter chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
            Topic updatedTopic = topicMapper.toEntity(existingTopicDTO, chapter);
            topicRepository.save(updatedTopic);
        } 
        else {
            // Save entire course DTO to temp table
            String json = objectMapper.writeValueAsString(courseDTO);
            appUtil.saveContentChange(json, courseId, instructorId, ContentAction.UPDATE);
        }
    }

    /**
     * Create a new topic
     */
    @Transactional
    public void createTopic(Long chapterId, TopicDTO topicDTO, Long instructorId) throws Exception {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
        
        Course course = chapter.getCourse();
        CourseDTO courseDTO = courseMapper.toDTO(course);
        
        // Set sequence to last + 1
        List<Topic> existingTopics = chapter.getTopics();
        int nextSequence = existingTopics.isEmpty() ? 1 : 
            existingTopics.stream()
                .mapToInt(Topic::getSequence)
                .max()
                .getAsInt() + 1;
        
        topicDTO.setSequence(nextSequence);
        
        // Always save new topic to main table first
        Topic newTopic = topicMapper.toEntity(topicDTO, chapter);
        if (newTopic.getTopicType() == TopicType.EXAM || newTopic.getTopicType() == TopicType.QUIZ) {
            if (newTopic.getQuestions() != null) {
                for (Question question : newTopic.getQuestions()) {
                    if (question.getAnswers() != null) {
                        question.getAnswers().forEach(answer -> answer.setQuestion(question));
                    }
                    question.setTopic(newTopic);
                }
            }
        }
        chapter.addTopic(newTopic);
        chapterRepository.save(chapter);
        
        // If course is not draft, save entire course DTO to temp table
        if (course.getContentStatus() != ContentStatus.DRAFT) {
            // Add new topic to course DTO
            ChapterDTO chapterDTO = AppUtil.getSelectChapterDTO(courseDTO, chapterId);
            chapterDTO.getTopics().add(topicDTO);
            
            // Save to temp table
            String json = objectMapper.writeValueAsString(courseDTO);
            appUtil.saveContentChange(json, course.getId(), instructorId, ContentAction.UPDATE);
        }
    }

    public void deleteTopic(Long courseId, Long chapterId, Long topicId, Long instructorId) throws Exception {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            if (course.getContentStatus() == ContentStatus.DRAFT) {
                topicRepository.deleteById(topicId);
            } else {
                CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);
                for (ChapterDTO chapterDTO : courseDTO.getChapters()) {
                    if (chapterDTO.getId() != null) {
                        if (chapterDTO.getId().equals(chapterId)) {
                            TopicDTO topicDTO = AppUtil.getSelectTopicDTO(chapterDTO, topicId);
                            chapterDTO.getTopics().remove(topicDTO);
                            AppUtil.reorderChapterDTOTopics(chapterDTO);
                            break;
                        }
                    }
                }
                try {   
                    String json = objectMapper.writeValueAsString(courseDTO);
                    appUtil.saveContentChange(json, course.getId(), instructorId, ContentAction.UPDATE);
                } catch (Exception e) {
                    throw new RuntimeException("Topic fail to delete.");
                }
            }
        }
    }
}
