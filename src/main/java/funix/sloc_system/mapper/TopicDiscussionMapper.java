package funix.sloc_system.mapper;

import funix.sloc_system.dto.TopicDiscussionDTO;
import funix.sloc_system.entity.TopicDiscussion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TopicDiscussionMapper {
    public TopicDiscussionDTO toDTO(TopicDiscussion topicDiscussion) {
        if (topicDiscussion == null) {
            return null;
        }

        TopicDiscussionDTO dto = new TopicDiscussionDTO();
        dto.setId(topicDiscussion.getId());
        dto.setTitle(topicDiscussion.getTitle());
        dto.setContent(topicDiscussion.getContent());
        dto.setTopicId(topicDiscussion.getTopic().getId());
        dto.setCreatedById(topicDiscussion.getCreatedBy().getId());
        dto.setCreatedByUsername(topicDiscussion.getCreatedBy().getUsername());
        dto.setCreatedAt(topicDiscussion.getCreatedAt());
        dto.setUpdatedAt(topicDiscussion.getUpdatedAt());
        dto.setCommentCount(topicDiscussion.getComments().size());

        return dto;
    }

    public List<TopicDiscussionDTO> toDTO(List<TopicDiscussion> topicDiscussions) {
        if (topicDiscussions == null) {
            return null;
        }
        return topicDiscussions.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
} 