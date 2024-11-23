package funix.sloc_system.service;

import funix.sloc_system.dao.QuizDao;
import funix.sloc_system.entity.Answer;
import funix.sloc_system.entity.Question;
import funix.sloc_system.entity.Quiz;
import funix.sloc_system.entity.QuizResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class QuizService {

    @Autowired
    private QuizDao quizDao;

    public Quiz getQuizById(Long quizId) {
        return quizDao.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    public QuizResult calculateScore(Long quizId, Map<String, String> answers) {
        Quiz quiz = getQuizById(quizId);

        int totalScore = 0;

        for (Question question : quiz.getQuestions()) {
            String selectedAnswerId = answers.get("question_" + question.getId());

            if (selectedAnswerId != null) {
                for (Answer answer : question.getAnswers()) {
                    if (answer.getId().toString().equals(selectedAnswerId) && answer.isCorrect()) {
                        totalScore += 1;
                        break;
                    }
                }
            }
        }

        boolean passed = totalScore >= quiz.getPassScore();

        return new QuizResult(totalScore, passed);
    }
}
