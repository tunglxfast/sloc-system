package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.entity.Course;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.mapper.TopicMapper;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.TopicRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class TopicService {
    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private TopicMapper topicMapper;
    
    @Autowired
    private ContentChangeService contentChangeService;
    
    @Autowired
    private ObjectMapper objectMapper;

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
        
        Optional<String> editingChanges = contentChangeService.getEntityEditing(EntityType.TOPIC, id)
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
    public void saveTopicChanges(TopicDTO topicDTO, Long instructorId) throws IOException {
        Topic topic = topicRepository.findById(topicDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        
        Course course = topic.getChapter().getCourse();
        if (course.getContentStatus() == ContentStatus.DRAFT) {
            // Save directly to main table
            Topic updatedTopic = topicMapper.toEntity(topicDTO);
            updatedTopic.setChapter(topic.getChapter());
            topicRepository.save(updatedTopic);
        } else {
            // Save to temporary table
            contentChangeService.saveEditingTopic(topicDTO, ContentAction.UPDATE, instructorId);
        }
    }
}
