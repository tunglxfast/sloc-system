package funix.sloc_system.service;

import funix.sloc_system.entity.Topic;
import funix.sloc_system.dao.TopicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {
    @Autowired
    private TopicDAO topicRepository;

    public Topic getTopicById(Long id) {
        return topicRepository.findById(id).orElse(null);
    }

    public List<Topic> getTopicsByChapter(Long chapterId) {
        return topicRepository.findByChapterId(chapterId);
    }
}
