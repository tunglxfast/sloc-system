package funix.sloc_system.service;

import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.util.AppUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class ChapterServiceTest {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private AppUtil appUtil;

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ChapterRepository chapterRepository;


    @Test
    public void testFindById() {
        Long chapterId = 1L;
        Chapter chapter = chapterService.findById(chapterId);
        assertNotNull(chapter);
        assertEquals(chapterId, chapter.getId());
    }

    @Test
    public void testFindByCourseId() {
        Long courseId = 1L;
        List<Chapter> chapters = chapterService.findByCourseId(courseId);
        assertNotNull(chapters);
        assertFalse(chapters.isEmpty());
    }

    @Test
    public void testFindByCourseAndSequence() {
        Long courseId = 1L;
        int sequence = 1;
        Chapter chapter = chapterService.findByCourseAndSequence(courseId, sequence);
        assertNotNull(chapter);
        assertEquals(sequence, chapter.getSequence());
    }

    @Test
    public void testCreateChapter() throws Exception {
        Long courseId = 1L;
        String title = "New Chapter";
        Long instructorId = 1L;

        assertDoesNotThrow(() -> chapterService.createChapter(courseId, title, instructorId));
        Course course = courseRepository.findById(courseId).orElse(null);
        List<Chapter> chapters = course.getChapters();
        Chapter createdChapter = null;
        for (Chapter chapter : chapters) {
          if (chapter.getTitle().equals(title)) {
            createdChapter = chapter;
          }
        }
        assertNotNull(createdChapter);
        assertEquals(title, createdChapter.getTitle());
    }

    @Test
    public void testUpdateChapter() throws Exception {
        Long courseId = 1L;
        Long chapterId = 1L;
        String newTitle = "Updated Chapter";
        Long instructorId = 1L;

        assertDoesNotThrow(() -> chapterService.updateChapter(courseId, chapterId, newTitle, instructorId));
        Chapter updatedChapter = chapterService.findById(chapterId);
        assertNotNull(updatedChapter);
        assertNotEquals(newTitle, updatedChapter.getTitle());
        CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);
        List<ChapterDTO> chapterDTOs = courseDTO.getChapters();

        boolean haveNewTitle = false;
        for (ChapterDTO chapterDTO : chapterDTOs) {
          if (chapterDTO.getTitle().equals(newTitle)) {
            haveNewTitle = true;
          }
        }
        assertTrue(haveNewTitle);
    }

    @Test
    public void testUpdateChapter_CourseContentStatusDraft() throws Exception {
        Long courseId = 1L;
        Long chapterId = 1L;
        String newTitle = "Updated Chapter";
        Long instructorId = 1L;

        Course course = courseRepository.findById(courseId).orElse(null);
        course.setContentStatus(ContentStatus.DRAFT);
        courseRepository.save(course);

        assertDoesNotThrow(() -> chapterService.updateChapter(courseId, chapterId, newTitle, instructorId));
        Chapter updatedChapter = chapterService.findById(chapterId);
        assertNotNull(updatedChapter);
        assertEquals(newTitle, updatedChapter.getTitle());
    }

    @Test
    public void testSaveOrUpdateChapters() {
        Long courseId = 1L;
        List<String> titles = List.of("Chapter 1", "Chapter 2");
        List<Long> chapterIds = List.of(1L, -2L);

        List<ChapterDTO> savedChapters = chapterService.saveOrUpdateChapters(courseId, titles, chapterIds);
        assertNotNull(savedChapters);
        assertEquals(titles.size(), savedChapters.size());
        assertEquals(savedChapters.get(0).getTitle(), titles.get(0));
        assertEquals(savedChapters.get(1).getTitle(), titles.get(1));
    }

    @Test
    public void testFindByTopicIdAsDTO() {
        Long topicId = 1L;
        ChapterDTO chapterDTO = chapterService.findByTopicIdAsDTO(topicId);
        assertNotNull(chapterDTO);
    }

    @Test
    public void testAddTopic() {
        Long chapterId = 1L;
        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setTitle("New Topic");
        topicDTO.setTopicType(TopicType.READING.name());
        topicDTO.setDescription("New Description");

        ChapterDTO updatedChapter = chapterService.addTopic(chapterId, topicDTO);
        assertNotNull(updatedChapter);
        assertEquals("New Topic", updatedChapter.getTopics().get(updatedChapter.getTopics().size() - 1).getTitle());
    }

    @Test
    public void testGetChaptersByCourseIdAsDTO() {
        Long courseId = 1L;
        List<ChapterDTO> chapters = chapterService.getChaptersByCourseIdAsDTO(courseId);
        assertNotNull(chapters);
        assertFalse(chapters.isEmpty());
    }

    @Test
    public void testDeleteChapter() throws Exception {
        Long courseId = 1L;
        Long chapterId = 1L;
        Long instructorId = 1L;

        assertDoesNotThrow(() -> chapterService.deleteChapter(courseId, chapterId, instructorId));
        assertNotNull(chapterService.findById(chapterId));

        CourseDTO courseDTO = appUtil.getEditingCourseDTO(courseId);
        List<ChapterDTO> chapterDTOs = courseDTO.getChapters();

        boolean chapterExist = false;
        for (ChapterDTO chapterDTO : chapterDTOs) {
          if (chapterDTO.getId().equals(chapterId)) {
            chapterExist = true;
          }
        }
        assertTrue(chapterExist == false);
    }

    @Test
    public void testDeleteChapter_CourseDraft() throws Exception {
        Long courseId = 1L;
        Long chapterId = 1L;
        Long instructorId = 1L;

        Course course = courseRepository.findById(courseId).orElse(null);
        course.setContentStatus(ContentStatus.DRAFT);
        courseRepository.save(course);

        assertDoesNotThrow(() -> chapterService.deleteChapter(courseId, chapterId, instructorId));
        assertNull(chapterService.findById(chapterId));
    }
} 