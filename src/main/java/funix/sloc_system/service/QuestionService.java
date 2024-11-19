package funix.sloc_system.service;

import funix.sloc_system.entity.Question;
import funix.sloc_system.dao.QuestionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {
    @Autowired
    private QuestionDAO questionDAO;

    public List<Question> getQuestionsByTopic(Long topicId) {
        return questionDAO.findByTopicId(topicId);
    }
}

