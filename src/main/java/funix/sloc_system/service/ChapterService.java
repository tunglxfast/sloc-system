package funix.sloc_system.service;

import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Course;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.CourseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private CourseRepository courseRepository;

    public Chapter getChapterById(Long id) {
        return chapterRepository.findById(id).orElse(null);
    }

    public Set<Chapter> getChaptersByCourse(Long courseId) {
        return chapterRepository.findByCourseId(courseId);
    }

    public Chapter findByCourseIdAndSequence(Long courseId, int chapterSequence) {
        return chapterRepository.findByCourseIdAndSequence(courseId, chapterSequence).orElse(null);
    }

    public Chapter createChapter(Long courseId, String title, int sequence) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Can not create chapter cause course id not exist"));

        Chapter chapter = new Chapter();
        chapter.setCourse(course);
        chapter.setTitle(title);
        chapter.setSequence(sequence);
        return chapterRepository.save(chapter);
    }

    public List<Chapter> getChaptersByCourseId(Long courseId) {
        return chapterRepository.findByCourseIdOrderBySequence(courseId);
    }

    public void createChapter(Long courseId, String title) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        int nextSequence = chapterRepository.findByCourseIdOrderBySequence(courseId).size() + 1;

        Chapter newChapter = new Chapter();
        newChapter.setCourse(course);
        newChapter.setTitle(title);
        newChapter.setSequence(nextSequence);

        chapterRepository.save(newChapter);
    }

    public void saveOrUpdateChapters(Long courseId, List<String> titles, List<Long> chapterIds) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        for (int i = 0; i < titles.size(); i++) {
            Chapter chapter;

            if (chapterIds.get(i) != null && chapterIds.get(i) > 0) {
                chapter = chapterRepository.findById(chapterIds.get(i))
                        .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
            } else {
                chapter = new Chapter();
                chapter.setCourse(course);
                chapter.setSequence(i + 1); // Tự động gán thứ tự
            }

            chapter.setTitle(titles.get(i));
            chapterRepository.save(chapter);
        }
    }

    public Chapter findById(Long chapterId) {
        return chapterRepository.findById(chapterId).orElse(null);
    }

    public Chapter addTopic(Long chapterId, Topic newTopic) {
        Chapter chapter = chapterRepository.findById(chapterId).
                orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
        chapter.getTopics().add(newTopic);
        return chapterRepository.save(chapter);
    }
}
