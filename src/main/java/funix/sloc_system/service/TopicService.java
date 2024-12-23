package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.mapper.TopicMapper;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.ContentChangeRepository;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.util.ApplicationUtil;
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
    private ContentChangeRepository contentChangeRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ApplicationUtil appUtil;

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
     * Reorder topics after sequence change
     */
    @Transactional
    private void reorderTopics(Long chapterId, Topic modifiedTopic, int newSequence) {
        int oldSequence = modifiedTopic.getSequence();
        
        // Temporarily move topic to a sequence outside the range (total topics + 1)
        int tempSequence = topicRepository.findByChapterId(chapterId).size() + 1;
        modifiedTopic.setSequence(tempSequence);
        topicRepository.save(modifiedTopic);
        
        // Get all topics that need to be shifted
        List<Topic> topics = topicRepository.findByChapterId(chapterId);
        
        if (newSequence < oldSequence) {
            // Moving up: shift topics between new and old position down
            topics.stream()
                .filter(t -> t.getSequence() >= newSequence && t.getSequence() < oldSequence)
                .forEach(t -> {
                    t.setSequence(t.getSequence() + 1);
                    topicRepository.save(t);
                });
        } else {
            // Moving down: shift topics between old and new position up
            topics.stream()
                .filter(t -> t.getSequence() > oldSequence && t.getSequence() <= newSequence)
                .forEach(t -> {
                    t.setSequence(t.getSequence() - 1);
                    topicRepository.save(t);
                });
        }
        
        // Set the modified topic to its new sequence
        modifiedTopic.setSequence(newSequence);
        topicRepository.save(modifiedTopic);
    }

    /**
     * Save topic changes based on course status
     */
    @Transactional
    public void saveTopicChanges(TopicDTO topicDTO, Long instructorId) throws IOException {
        Topic topic = topicRepository.findById(topicDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        
        if (topic.getContentStatus() == ContentStatus.DRAFT) {
            int oldTopicSequence = topic.getSequence();
            // Update topic using entity method
            Topic updatedTopic = topicMapper.toEntity(topicDTO);
            topic.updateWithOtherTopic(updatedTopic);
            // Handle sequence change if needed
            if (oldTopicSequence != topicDTO.getSequence()) {
                // Validate new sequence
                int maxSequence = topicRepository.findByChapterId(topic.getChapter().getId()).size();
                int newSequence = Math.max(1, Math.min(topicDTO.getSequence(), maxSequence));
                
                // Reorder topics if sequence changed
                reorderTopics(topic.getChapter().getId(), topic, newSequence);
            }            
            topicRepository.save(topic);
        } else {
            // Save to temporary table
            appUtil.saveEntityChanges(EntityType.TOPIC, topicDTO, topic, ContentAction.UPDATE, instructorId);
        }
    }
}
