package funix.sloc_system.service;

import funix.sloc_system.dao.ChapterDao;
import funix.sloc_system.dao.TopicDao;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        Chapter chapter = chapterDao.findByCourseIdAndSequence(courseId, chapterSequence)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        return topicDao.findByChapterIdAndSequence(chapter.getId(), topicSequence)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
    }
}
