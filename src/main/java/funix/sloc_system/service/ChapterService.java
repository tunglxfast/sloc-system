package funix.sloc_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.mapper.ChapterMapper;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.mapper.TopicMapper;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.ContentChangeRepository;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.util.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChapterService {
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private ChapterMapper chapterMapper;
    
    @Autowired
    private TopicMapper topicMapper;
    
    @Autowired
    private ContentChangeRepository contentChangeRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUtil appUtil;

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
    public void createChapter(Long courseId, String title, Long instructorId) throws Exception {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);

        int newSequence = chapterRepository.findByCourseIdOrderBySequence(courseId).size() + 1;

        // Create new chapter, always save new entity directly to table
        Chapter newChapter = new Chapter();
        newChapter.setCourse(course);
        newChapter.setTitle(title);
        newChapter.setSequence(newSequence);
        newChapter.setContentStatus(ContentStatus.DRAFT);
        chapterRepository.save(newChapter);

        if (!ContentStatus.DRAFT.name().equals(courseDTO.getContentStatus())) {
            ChapterDTO newChapterDTO = chapterMapper.toDTO(newChapter);
            courseDTO.getChapters().add(newChapterDTO);
            try {
                String json = objectMapper.writeValueAsString(courseDTO);
                appUtil.saveContentChange(json, courseId, instructorId);
            } catch (Exception e) {
                throw new RuntimeException("Chapter fail to create.");// TODO: handle exception
            }
        }
    }

    /**
     * Save chapter changes based on course status
     */
    @Transactional
    public void updateChapter(Long courseId, Long chapterId, String title, Long instructorId) throws Exception {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);
        ChapterDTO chapterDTO = AppUtil.getSelectChapterDTO(courseDTO, chapterId);

        chapterDTO.setTitle(title);

        if (ContentStatus.DRAFT.name().equals(courseDTO.getContentStatus())) {
            // Save chapter changes direct to table if course is DRAFT
            Chapter chapter = chapterMapper.toEntity(chapterDTO, course);
            chapterRepository.save(chapter);
        } else {
            // Save chapter changes to temp table.
            String json = objectMapper.writeValueAsString(courseDTO);
            appUtil.saveContentChange(json, courseId, instructorId);
        }
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
     * Find chapter by topic ID and return as DTO
     */
    public ChapterDTO findByTopicIdAsDTO(Long topicId) {
        Chapter chapter = chapterRepository.findByTopicsId(topicId)
                .orElse(null);
        return chapter != null ? chapterMapper.toDTO(chapter) : null;
    }

    /**
     * Add a new topic to a chapter
     */
    @Transactional
    public ChapterDTO addTopic(Long chapterId, TopicDTO topicDTO) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
        
        // Set sequence to last + 1
        List<Topic> existingTopics = chapter.getTopics();
        int nextSequence = existingTopics.isEmpty() ? 1 : 
            existingTopics.stream()
                .mapToInt(Topic::getSequence)
                .max()
                .getAsInt() + 1;
        
        topicDTO.setSequence(nextSequence);
        
        Topic newTopic = topicMapper.toEntity(topicDTO, chapter);
        newTopic.setChapter(chapter);
        chapter.addTopic(newTopic);
        
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

    /**
     * Reorder chapters after sequence change
     */
    @Transactional
    private void reorderChapters(Long courseId, Chapter modifiedChapter, int newSequence) {
        int oldSequence = modifiedChapter.getSequence();
        
        // Temporarily move chapter to a sequence outside the range (total chapters + 1)
        int tempSequence = chapterRepository.findByCourseId(courseId).size() + 1;
        modifiedChapter.setSequence(tempSequence);
        chapterRepository.save(modifiedChapter);
        
        // Get all chapters that need to be shifted
        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);
        
        if (newSequence < oldSequence) {
            // Moving up: shift chapters between new and old position down
            chapters.stream()
                .filter(c -> c.getSequence() >= newSequence && c.getSequence() < oldSequence)
                .forEach(c -> {
                    c.setSequence(c.getSequence() + 1);
                    chapterRepository.save(c);
                });
        } else {
            // Moving down: shift chapters between old and new position up
            chapters.stream()
                .filter(c -> c.getSequence() > oldSequence && c.getSequence() <= newSequence)
                .forEach(c -> {
                    c.setSequence(c.getSequence() - 1);
                    chapterRepository.save(c);
                });
        }
        
        // Set the modified chapter to its new sequence
        modifiedChapter.setSequence(newSequence);
        chapterRepository.save(modifiedChapter);
    }
}
