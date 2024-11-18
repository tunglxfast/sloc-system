package funix.sloc_system.service;

import funix.sloc_system.entity.Chapter;
import funix.sloc_system.repository.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;

    public Chapter getChapterById(Long id) {
        return chapterRepository.findById(id).orElse(null);
    }

    public List<Chapter> getChaptersByCourse(Long courseId) {
        return chapterRepository.findByCourseId(courseId);
    }
}
