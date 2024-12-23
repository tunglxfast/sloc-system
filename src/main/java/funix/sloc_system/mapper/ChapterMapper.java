package funix.sloc_system.mapper;

import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChapterMapper {
    
    @Autowired
    private TopicMapper topicMapper;
    @Autowired
    private CourseRepository courseRepository;

    public ChapterDTO toDTO(Chapter chapter) {
        if (chapter == null) {
            return null;
        }

        ChapterDTO dto = new ChapterDTO();
        dto.setId(chapter.getId());
        dto.setTitle(chapter.getTitle());
        dto.setContentStatus(chapter.getContentStatus().toString());
        dto.setSequence(chapter.getSequence());
        dto.setCourseId(chapter.getCourse().getId());
        
        // Map topics
        if (chapter.getTopics() != null) {
            dto.setTopics(topicMapper.toDTO(chapter.getTopics()));
        }
        
        return dto;
    }

    public Chapter toEntity(ChapterDTO dto) {
        if (dto == null) {
            return null;
        }

        Chapter chapter = new Chapter();
        chapter.setId(dto.getId());
        chapter.setTitle(dto.getTitle());
        chapter.setContentStatus(ContentStatus.valueOf(dto.getContentStatus()));
        chapter.setSequence(dto.getSequence());
        chapter.setCourse(courseRepository.findById(dto.getCourseId()).orElse(null));
        
        // Map topics if present
        if (dto.getTopics() != null) {
            chapter.setTopics(topicMapper.toEntity(dto.getTopics()));
        }
        
        return chapter;
    }

    public List<ChapterDTO> toDTO(List<Chapter> chapters) {
        return chapters.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<Chapter> toEntity(List<ChapterDTO> chapterDTOList) {
        return chapterDTOList.stream().map(this::toEntity).collect(Collectors.toList());
    }
} 