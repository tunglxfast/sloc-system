package funix.sloc_system.mapper;

import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChapterMapper {
    
    @Autowired
    private TopicMapper topicMapper;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ChapterRepository chapterRepository;


    public ChapterDTO toDTO(Chapter chapter) {
        if (chapter == null) {
            return null;
        }

        ChapterDTO dto = new ChapterDTO();
        dto.setId(chapter.getId());
        dto.setTitle(chapter.getTitle());
        if (chapter.getContentStatus() != null) {
            dto.setContentStatus(chapter.getContentStatus().toString());
        }
        dto.setSequence(chapter.getSequence());

        if (chapter.getCourse() != null) {
            dto.setCourseId(chapter.getCourse().getId());
        }
        
        // Map topics
        if (chapter.getTopics() != null) {
            for (Topic topic : chapter.getTopics()) {
                dto.addTopic(topicMapper.toDTO(topic));
            }
        }
        
        return dto;
    }

    public Chapter toEntity(ChapterDTO dto, Course course) {
        if (dto == null) {
            return null;
        }

        Chapter chapter;
        if (dto.getId() != null) {
            chapter = chapterRepository.findById(dto.getId()).orElse(new Chapter());
        } else {
            chapter = new Chapter();
        }

        if (chapter.getId() == null && dto.getId() != null) {
            chapter.setId(dto.getId());
        }

        chapter.setTitle(dto.getTitle());
        chapter.setSequence(dto.getSequence());

        chapter.setCourse(course);

        if (dto.getContentStatus() != null && !dto.getContentStatus().isBlank()){
            chapter.setContentStatus(ContentStatus.valueOf(dto.getContentStatus()));
        }

        // Map topics if present
        if (dto.getTopics() != null) {
            if (chapter.getTopics() != null) {
                chapter.getTopics().clear();
            } else {
                chapter.setTopics(new ArrayList<>());
            }
            for (TopicDTO topicDTO : dto.getTopics()) {
                chapter.addTopic(topicMapper.toEntity(topicDTO, chapter));
            }
        }
        
        return chapter;
    }

    public List<ChapterDTO> toDTO(List<Chapter> chapters) {
        return chapters.stream().map(chapter -> toDTO(chapter)).collect(Collectors.toList());
    }

    public List<Chapter> toEntity(List<ChapterDTO> chapterDTOList, Course course) {
        return chapterDTOList.stream().map(dto -> toEntity(dto, course)).collect(Collectors.toList());
    }
} 