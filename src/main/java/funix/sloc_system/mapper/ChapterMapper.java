package funix.sloc_system.mapper;

import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.enums.CourseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChapterMapper {
    
    @Autowired
    private TopicMapper topicMapper;

    public ChapterDTO toDTO(Chapter chapter) {
        if (chapter == null) {
            return null;
        }

        ChapterDTO dto = new ChapterDTO();
        dto.setId(chapter.getId());
        dto.setTitle(chapter.getTitle());
        dto.setStatus(chapter.getStatus().toString());
        dto.setSequence(chapter.getSequence());
        
        // Map topics
        dto.setTopics(topicMapper.toDTO(chapter.getTopics()));
        
        return dto;
    }

    public Chapter toEntity(ChapterDTO dto) {
        if (dto == null) {
            return null;
        }

        Chapter chapter = new Chapter();
        chapter.setId(dto.getId());
        chapter.setTitle(dto.getTitle());
        chapter.setStatus(CourseStatus.valueOf(dto.getStatus()));
        chapter.setSequence(dto.getSequence());
        
        // Map topics if present
        if (dto.getTopics() != null) {
            chapter.setTopics(topicMapper.toEntity(dto.getTopics()));
        }
        
        return chapter;
    }

    public List<ChapterDTO> toDTO(List<Chapter> chapters) {
        return chapters.stream().map(this::toDTO).toList();
    }

    public List<Chapter> toEntity(List<ChapterDTO> chapterDTOList) {
        return chapterDTOList.stream().map(this::toEntity).toList();
    }
} 