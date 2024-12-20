package funix.sloc_system.service;

import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TopicService {
    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    public Topic findById(Long id) {
        return topicRepository.findById(id).orElse(null);
    }

    public List<Topic> findByChapterId(Long chapterId) {
        return topicRepository.findByChapterId(chapterId);
    }

    public Topic findByChapterAndTopicSequence(Long courseId, int chapterSequence, int topicSequence) {
        Chapter chapter = chapterRepository.findByCourseIdAndSequence(courseId, chapterSequence).orElse(null);
        if (chapter == null) {
            return null;
        }

        Optional<Topic> topic = topicRepository.findByChapterIdAndSequence(chapter.getId(), topicSequence);
        if (topic.isEmpty()) {
            return null;
        } else {
            return topic.get();
        }
    }
}
