package funix.sloc_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import funix.sloc_system.enums.ContentStatus;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDTO {
    private Long id;
    private String title;
    private int sequence;
    private Long courseId;
    private List<TopicDTO> topics;
    private String contentStatus;

    // Helper methods
    public void addTopic(TopicDTO topicDTO) {
        if (topics == null) {
            topics = new ArrayList<>();
        }
        if (topics.contains(topicDTO)) {
            return;
        }
        topicDTO.setChapterId(id);
        topics.add(topicDTO);
    }
}
