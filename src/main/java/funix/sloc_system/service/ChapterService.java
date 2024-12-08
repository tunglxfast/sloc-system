package funix.sloc_system.service;

import funix.sloc_system.dao.ChapterDao;
import funix.sloc_system.dao.CourseDao;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChapterService {

    @Autowired
    private ChapterDao chapterDAO;
    @Autowired
    private CourseDao courseDao;

    public Chapter getChapterById(Long id) {
        return chapterDAO.findById(id).orElse(null);
    }

    public List<Chapter> getChaptersByCourse(Long courseId) {
        return chapterDAO.findByCourseId(courseId);
    }

    public Chapter findByCourseIdAndSequence(Long courseId, int chapterSequence) {
        return chapterDAO.findByCourseIdAndSequence(courseId, chapterSequence).orElse(null);
    }

    public Chapter createChapter(Long courseId, String title, int sequence) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Can not create chapter cause course id not exist"));

        Chapter chapter = new Chapter();
        chapter.setCourse(course);
        chapter.setTitle(title);
        chapter.setSequence(sequence);
        return chapterDAO.save(chapter);
    }

    public List<Chapter> getChaptersByCourseId(Long courseId) {
        return chapterDAO.findByCourseIdOrderBySequence(courseId);
    }

    public void saveOrUpdateChapters(Long courseId, List<String> titles, List<Long> chapterIds) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        for (int i = 0; i < titles.size(); i++) {
            Chapter chapter;

            if (chapterIds.get(i) != null && chapterIds.get(i) > 0) {
                chapter = chapterDAO.findById(chapterIds.get(i))
                        .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
            } else {
                chapter = new Chapter();
                chapter.setCourse(course);
                chapter.setSequence(i + 1); // Tự động gán thứ tự
            }

            chapter.setTitle(titles.get(i));
            chapterDAO.save(chapter);
        }
    }
}
