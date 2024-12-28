package funix.sloc_system.service;

import funix.sloc_system.repository.*;
import funix.sloc_system.dto.TestResultDTO;
import funix.sloc_system.entity.*;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.mapper.TestResultMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestResultService {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private TestResultMapper testResultMapper;

    public TestResultDTO findByUserIdAndTopicId(Long userId, Long topicId) {
        TestResult result = testResultRepository.findByUserIdAndTopicId(userId, topicId).orElse(null);
        return testResultMapper.toDTO(result);
    }

    @Transactional
    public TestResultDTO calculateScore(Long userId, Long topicId, Map<String, String> answers) {
        User user = userRepository.findById(userId).orElse(null);
        Topic topic = topicRepository.findById(topicId).orElse(null);
        TopicType topicType = topic.getTopicType();
        if (user == null || topic == null) {
            return null;
        }

        if (!topicType.equals(TopicType.EXAM) && !topicType.equals(TopicType.QUIZ)) {
            return null;
        }

        int totalScore = 0;

        List<Answer> correctAnswers = new ArrayList<>();
        List<String> correctAnswerIds = new ArrayList<>();
        List<String> selectedAnswerIds = new ArrayList<>();
        List<Question> questions = topic.getQuestions();
        for (Question question : questions) {
            correctAnswers.clear();
            correctAnswerIds.clear();
            selectedAnswerIds.clear();

            selectedAnswerIds.addAll(
                    answers.entrySet().stream()
                            .filter(entry -> entry.getKey().startsWith("question_" + question.getId()))
                            .map(stringEntry -> stringEntry.getValue()).collect(Collectors.toList()))
            ;
            correctAnswers.addAll(answerRepository.findByQuestionIdAndCorrectTrue(question.getId()));
            correctAnswerIds.addAll(correctAnswers.stream().map(answer -> answer.getId().toString()).collect(Collectors.toList()));

            if (new HashSet<>(selectedAnswerIds).containsAll(correctAnswerIds)
                    && new HashSet<>(correctAnswerIds).containsAll(selectedAnswerIds)) {
                totalScore += 1;
            }
        }

        int passScore = topic.getPassScore();
        boolean isPassed = totalScore >= passScore;

        TestResult testResult = testResultRepository.findByUserIdAndTopicId(userId, topicId).orElse(null);
        if (testResult == null){
            testResult = new TestResult(totalScore, totalScore, isPassed, 1, topicType, user, topic);
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
            if (!testResult.getPassed()) {
                testResult.setPassed(isPassed);
            }
        }
        testResult = testResultRepository.save(testResult);
        return testResultMapper.toDTO(testResult);
    }
}
