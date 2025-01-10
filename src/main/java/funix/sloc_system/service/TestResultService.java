package funix.sloc_system.service;

import funix.sloc_system.repository.*;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.TestResultDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.*;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.mapper.TestResultMapper;
import funix.sloc_system.mapper.TopicMapper;
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

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private DTOService dtoService;


    public TestResultDTO findByUserIdAndTopicId(Long userId, Long topicId) {
        TestResult result = testResultRepository.findByUserIdAndTopicId(userId, topicId).orElse(null);
        return testResultMapper.toDTO(result);
    }

    @Transactional
    public TestResultDTO calculateScore(Long userId, Long topicId, Map<String, String> answers) {
        User user = userRepository.findById(userId).orElse(null);
        Topic topicEntity = topicRepository.findById(topicId).orElse(null);
        if (user == null || topicEntity == null) {
            return null;
        }

        TopicType topicType = topicEntity.getTopicType();
        if (!topicType.equals(TopicType.EXAM) && !topicType.equals(TopicType.QUIZ)) {
            return null;
        }

        TopicDTO topicDTO = topicMapper.toDTO(topicEntity);
        topicDTO.setQuestions(dtoService.getAvailableQuestions(topicDTO));

        // 0 - 100
        double totalScore = 0;
        double countCorrectAnswer = 0;

        List<Answer> correctAnswers = new ArrayList<>();
        List<String> correctAnswerContents = new ArrayList<>();
        List<String> selectedAnswerContents = new ArrayList<>();
        List<QuestionDTO> questions = topicDTO.getQuestions();

        for (QuestionDTO question : questions) {
            correctAnswers.clear();
            correctAnswerContents.clear();
            selectedAnswerContents.clear();

            selectedAnswerContents.addAll(
                    answers.entrySet().stream()
                            .filter(entry -> entry.getKey().startsWith("question_" + question.getId()))
                            .map(stringEntry -> stringEntry.getValue()).collect(Collectors.toList()))
            ;
            correctAnswers.addAll(answerRepository.findByQuestionIdAndCorrectTrue(question.getId()));
            for (Answer answer : correctAnswers) {
                if (answer.getId() == null) {
                    continue;
                }
                correctAnswerContents.add(answer.getContent());
            }

            if (new HashSet<>(selectedAnswerContents).containsAll(correctAnswerContents)
                    && new HashSet<>(correctAnswerContents).containsAll(selectedAnswerContents)) {
                countCorrectAnswer += 1;
            }
        }

         // 0 - 100
        int passScore = topicDTO.getPassScore();
        totalScore = Math.round(countCorrectAnswer / questions.size() * 100);
        boolean isPassed = totalScore >= passScore;

        TestResult testResult = testResultRepository.findByUserIdAndTopicId(userId, topicId).orElse(null);
        if (testResult == null){
            testResult = new TestResult(totalScore, totalScore, isPassed, 1, topicType, user, topicEntity);
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
