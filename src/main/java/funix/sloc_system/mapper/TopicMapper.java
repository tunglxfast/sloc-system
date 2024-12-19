package funix.sloc_system.mapper;

import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.TopicType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class TopicMapper {
    
    @Autowired
    private QuestionMapper questionMapper;

    public TopicDTO toDTO(Topic topic) {
        if (topic == null) {
            return null;
        }

        TopicDTO dto = new TopicDTO();
        dto.setId(topic.getId());
        dto.setTitle(topic.getTitle());
        dto.setTopicType(topic.getTopicType().toString());
        dto.setSequence(topic.getSequence());
        
        // Map questions
        dto.setQuestions(topic.getQuestions().stream()
                .map(questionMapper::toDTO)
                .toList());
        
        return dto;
    }

    public Topic toEntity(TopicDTO dto) {
        if (dto == null) {
            return null;
        }

        Topic topic = new Topic();
        topic.setId(dto.getId());
        topic.setTitle(dto.getTitle());
        topic.setSequence(dto.getSequence());
        
        // Convert string topicType to enum
        if (dto.getTopicType() != null) {
            topic.setTopicType(TopicType.valueOf(dto.getTopicType()));
        }
        
        // Map questions if present
        if (dto.getQuestions() != null) {
            topic.setQuestions(dto.getQuestions().stream()
                    .map(questionMapper::toEntity)
                    .toList());
        } else {
            topic.setQuestions(new ArrayList<>());
        }
        
        return topic;
    }
} 