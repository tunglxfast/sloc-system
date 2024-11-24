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

    public Topic getTopicById(Long id) {
        return topicDao.findById(id).orElse(null);
    }

    public List<Topic> getTopicsByChapter(Long chapterId) {
        return topicDao.findByChapterId(chapterId);
    }

    public Topic getTopicByChapterAndTopicSequence(Long courseId, int chapterSequence, int topicSequence) {
        Optional<Chapter> chapter = chapterDao.findByCourseIdAndSequence(courseId, chapterSequence);
        if (chapter.isEmpty()) {
            return null;
        }

        Optional<Topic> topic = topicDao.findByChapterIdAndSequence(chapter.get().getId(), topicSequence);
        if (topic.isEmpty()) {
            return null;
        } else {
            return topic.get();
        }
    }
}
