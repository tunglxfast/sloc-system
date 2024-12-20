package funix.sloc_system.mapper;

import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.TopicType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        dto.setQuestions(questionMapper.toDTO(topic.getQuestions()));
        
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
            topic.setQuestions(questionMapper.toEntity(dto.getQuestions()));
        } else {
            topic.setQuestions(new ArrayList<>());
        }
        
        return topic;
    }

    public List<TopicDTO> toDTO(List<Topic> topics) {
        return topics.stream().map(this::toDTO).toList();
    }

    public List<Topic> toEntity(List<TopicDTO> topicDTOList) {
        return topicDTOList.stream().map(this::toEntity).toList();
    }
} 