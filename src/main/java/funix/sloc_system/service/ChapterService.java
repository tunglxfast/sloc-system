package funix.sloc_system.service;

import funix.sloc_system.dao.ChapterDao;
import funix.sloc_system.entity.Chapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChapterService {

    @Autowired
    private ChapterDao chapterDAO;

    public Chapter getChapterById(Long id) {
        return chapterDAO.findById(id).orElse(null);
    }

    public List<Chapter> getChaptersByCourse(Long courseId) {
        return chapterDAO.findByCourseId(courseId);
    }

    public Chapter findByCourseIdAndSequence(Long courseId, int chapterSequence) {
        return chapterDAO.findByCourseIdAndSequence(courseId, chapterSequence).orElse(null);
    }
}
