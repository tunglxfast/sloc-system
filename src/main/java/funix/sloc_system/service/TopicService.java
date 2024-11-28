package funix.sloc_system.service;

import funix.sloc_system.dao.ChapterDao;
import funix.sloc_system.dao.TopicDao;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TopicService {
    @Autowired
    private TopicDao topicDao;

    @Autowired
    private ChapterDao chapterDao;

    public Topic findById(Long id) {
        return topicDao.findById(id).orElse(null);
    }

    public List<Topic> findByChapterId(Long chapterId) {
        return topicDao.findByChapterId(chapterId);
    }

    public Topic findByChapterAndTopicSequence(Long courseId, int chapterSequence, int topicSequence) {
        Chapter chapter = chapterDao.findByCourseIdAndSequence(courseId, chapterSequence).orElse(null);
        if (chapter == null) {
            return null;
        }

        Optional<Topic> topic = topicDao.findByChapterIdAndSequence(chapter.getId(), topicSequence);
        if (topic.isEmpty()) {
            return null;
        } else {
            return topic.get();
        }
    }
}
