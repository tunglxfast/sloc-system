package funix.sloc_system.service;

import funix.sloc_system.dao.AnswerDao;
import funix.sloc_system.entity.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerService {
    @Autowired
    private AnswerDao answerDao;

    public List<Answer> findByQuestionIdAndIsCorrectTrue(Long questionId) {
        return answerDao.findByQuestionIdAndIsCorrectTrue(questionId);
    }
}
