package funix.sloc_system.mapper;

import funix.sloc_system.dto.CommentDTO;
import funix.sloc_system.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {
    public CommentDTO toDTO(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setTopicDiscussionId(comment.getTopicDiscussion().getId());
        dto.setCreatedById(comment.getCreatedBy().getId());
        dto.setCreatedByUsername(comment.getCreatedBy().getUsername());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        return dto;
    }

    public List<CommentDTO> toDTOs(List<Comment> comments) {
        return comments.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
} 