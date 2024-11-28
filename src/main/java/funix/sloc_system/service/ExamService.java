package funix.sloc_system.service;

import funix.sloc_system.dao.ExamDao;
import funix.sloc_system.dao.TestResultDao;
import funix.sloc_system.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ExamService {

    @Autowired
    private ExamDao examDao;

    @Autowired
    private TestResultDao examResultDao;

    public Exam getExamById(Long examId) {
        Optional<Exam> response = examDao.findById(examId);
        if (response.isEmpty()) {
            return null;
        } else {
            return response.get();
        }
    }

    public int calculateScore(Long examId, Map<String, String> answers) {
        Exam exam = getExamById(examId);

        int totalScore = 0;

        for (Question question : exam.getQuestions()) {
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
        return totalScore;
    }
}
