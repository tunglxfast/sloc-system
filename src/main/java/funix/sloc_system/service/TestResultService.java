package funix.sloc_system.service;

import funix.sloc_system.dao.*;
import funix.sloc_system.entity.*;
import funix.sloc_system.enums.TopicType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class TestResultService {

    @Autowired
    private TopicDao topicDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ExamDao examDao;

    @Autowired
    private QuizDao quizDao;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private TestResultDao testResultDao;

    public TestResult findByUserIdAndTopicId(Long userId, Long topicId) {
        return testResultDao.findByUserIdAndTopicId(userId, topicId).orElse(null);
    }

    @Transactional
    public TestResult calculateScore(Long userId, Long topicId, Map<String, String> answers) {
        User user = userDao.findById(userId).orElse(null);
        Topic topic = topicDao.findById(topicId).orElse(null);
        if (user == null || topic == null) {
            return null;
        }

        TopicType topicType = topic.getTopicType();
        boolean isQuiz = false;
        if (topicType.equals(TopicType.EXAM)) {
            isQuiz = false;
            return calculateScore(userId, topicId, answers, isQuiz);
        } else if (topicType.equals(TopicType.QUIZ)) {
            isQuiz = true;
            return calculateScore(userId, topicId, answers, isQuiz);
        } else {
            return null;
        }
    }

    @Transactional
    private TestResult calculateScore(Long userId, Long topicId, Map<String, String> answers, boolean isQuiz) {
        User user = userDao.findById(userId).orElse(null);
        Object topic = isQuiz ? quizDao.findById(topicId).orElse(null) : examDao.findById(topicId).orElse(null);

        if (user == null || topic == null) {
            return null;
        }
        int totalScore = 0;

        List<Answer> correctAnswers = new ArrayList<>();
        List<String> correctAnswerIds = new ArrayList<>();
        List<String> selectedAnswerIds = new ArrayList<>();
        List<Question> questions = isQuiz ? ((Quiz) topic).getQuestions() : ((Exam) topic).getQuestions();
        for (Question question : questions) {
            correctAnswers.clear();
            correctAnswerIds.clear();
            selectedAnswerIds.clear();

            selectedAnswerIds.addAll(
                    answers.entrySet().stream()
                            .filter(entry -> entry.getKey().startsWith("question_" + question.getId()))
                            .map(stringEntry -> stringEntry.getValue()).toList())
            ;
            correctAnswers.addAll(answerDao.findByQuestionIdAndIsCorrectTrue(question.getId()));
            correctAnswerIds.addAll(correctAnswers.stream().map(answer -> answer.getId().toString()).toList());

            if (new HashSet<>(selectedAnswerIds).containsAll(correctAnswerIds)
                    && new HashSet<>(correctAnswerIds).containsAll(selectedAnswerIds)) {
                totalScore += 1;
            }
        }

        int passScore = isQuiz ? ((Quiz) topic).getPassScore() : ((Exam) topic).getPassScore();
        boolean isPassed = totalScore >= passScore;

        String topicType = ((Topic) topic).getTopicType().toString();

        TestResult testResult = testResultDao.findByUserIdAndTopicId(userId, topicId).orElse(null);
        if (testResult == null){
            testResult = new TestResult(totalScore, totalScore, isPassed, 1, topicType, user, ((Topic) topic));
        } else {
            testResult.setLatestScore(totalScore);
            if (testResult.getParticipationCount() != null) {
                testResult.setParticipationCount(testResult.getParticipationCount()+1);
            } else {
                testResult.setParticipationCount(1);
            }
            if (testResult.getHighestScore() < totalScore) {
                testResult.setHighestScore(totalScore);
            }
            if (!testResult.isPassed()) {
                testResult.setPassed(isPassed);
            }
        }
        return testResultDao.save(testResult);
    }
}
