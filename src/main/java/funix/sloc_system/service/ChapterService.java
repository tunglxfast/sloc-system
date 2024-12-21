package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.mapper.ChapterMapper;
import funix.sloc_system.mapper.TopicMapper;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.ContentAction;
import funix.sloc_system.enums.EntityType;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.CourseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ChapterService {
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private ChapterMapper chapterMapper;
    
    @Autowired
    private TopicMapper topicMapper;
    
    @Autowired
    private ContentChangeService contentChangeService;
    
    @Autowired
    private ObjectMapper objectMapper;

    public Chapter findById(Long id) {
        return chapterRepository.findById(id).orElse(null);
    }

    public List<Chapter> findByCourseId(Long courseId) {
        return chapterRepository.findByCourseIdOrderBySequence(courseId);
    }

    public Chapter findByCourseAndSequence(Long courseId, int sequence) {
        return chapterRepository.findByCourseIdAndSequence(courseId, sequence).orElse(null);
    }

    /**
     * Create a new chapter for a course
     */
    @Transactional
    public ChapterDTO createChapter(Long courseId, String title) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // Get next sequence number
        int nextSequence = chapterRepository.findByCourseIdOrderBySequence(courseId).size() + 1;

        // Create new chapter
        Chapter newChapter = new Chapter();
        newChapter.setCourse(course);
        newChapter.setTitle(title);
        newChapter.setSequence(nextSequence);

        // Save and return as DTO
        Chapter savedChapter = chapterRepository.save(newChapter);
        return chapterMapper.toDTO(savedChapter);
    }

    /**
     * Save or update multiple chapters at once
     */
    @Transactional
    public List<ChapterDTO> saveOrUpdateChapters(Long courseId, List<String> titles, List<Long> chapterIds) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        List<Chapter> updatedChapters = new java.util.ArrayList<>();
        
        for (int i = 0; i < titles.size(); i++) {
            Chapter chapter;

            if (chapterIds.get(i) != null && chapterIds.get(i) > 0) {
                chapter = chapterRepository.findById(chapterIds.get(i))
                        .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
            } else {
                chapter = new Chapter();
                chapter.setCourse(course);
                chapter.setSequence(i + 1);
            }

            chapter.setTitle(titles.get(i));
            updatedChapters.add(chapterRepository.save(chapter));
        }

        return chapterMapper.toDTO(updatedChapters);
    }

    /**
     * Get editing changes for chapter
     */
    @Transactional
    public ChapterDTO getEditingChapterDTO(Long id) throws Exception {
        Chapter chapter = findById(id);
        if (chapter == null) {
            return null;
        }
        
        Optional<String> editingChanges = contentChangeService.getEntityEditing(EntityType.CHAPTER, id)
                .map(change -> change.getChanges());
        
        if (editingChanges.isPresent()) {
            return objectMapper.readValue(editingChanges.get(), ChapterDTO.class);
        } else {
            return chapterMapper.toDTO(chapter);
        }
    }

    /**
     * Find chapter by topic ID and return as DTO
     */
    public ChapterDTO findByTopicIdAsDTO(Long topicId) {
        Chapter chapter = chapterRepository.findByTopicsId(topicId)
                .orElse(null);
        return chapter != null ? chapterMapper.toDTO(chapter) : null;
    }

    /**
     * Save chapter changes based on course status
     */
    @Transactional
    public void saveChapterChanges(ChapterDTO chapterDTO, Long instructorId) throws IOException {
        Chapter chapter = chapterRepository.findById(chapterDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
        
        Course course = chapter.getCourse();
        if (course.getContentStatus() == ContentStatus.DRAFT) {
            // Save directly to main table
            Chapter updatedChapter = chapterMapper.toEntity(chapterDTO);
            updatedChapter.setCourse(course);
            chapterRepository.save(updatedChapter);
        } else {
            // Save to temporary table
            contentChangeService.saveEditingChapter(chapterDTO, ContentAction.UPDATE, instructorId);
        }
    }

    /**
     * Add a new topic to a chapter
     */
    @Transactional
    public ChapterDTO addTopic(Long chapterId, TopicDTO topicDTO) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
        
        Topic newTopic = topicMapper.toEntity(topicDTO);
        newTopic.setChapter(chapter);
        chapter.getTopics().add(newTopic);
        
        Chapter savedChapter = chapterRepository.save(chapter);
        return chapterMapper.toDTO(savedChapter);
    }

    /**
     * Get all chapters for a course ordered by sequence
     */
    public List<ChapterDTO> getChaptersByCourseIdAsDTO(Long courseId) {
        List<Chapter> chapters = chapterRepository.findByCourseIdOrderBySequence(courseId);
        return chapterMapper.toDTO(chapters);
    }
}
