package funix.sloc_system.service;

import funix.sloc_system.entity.Chapter;
import funix.sloc_system.dao.ChapterDao;
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
}
