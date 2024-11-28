package funix.sloc_system.service;

import funix.sloc_system.dao.QuizDao;
import funix.sloc_system.entity.Quiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QuizService {

    @Autowired
    private QuizDao quizDao;

    public Quiz getQuizById(Long quizId) {
        Optional<Quiz> response = quizDao.findById(quizId);
        if (response.isEmpty()) {
            return null;
        } else {
            return response.get();
        }
    }
}
