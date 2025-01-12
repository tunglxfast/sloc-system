package funix.sloc_system.service;

import funix.sloc_system.repository.*;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.TestResultDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.*;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.enums.QuestionType;
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
    private LearnedTopicRepository learnedTopicRepository;

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


    /*
     * Calculate the score of the test and 
     * update LearnedTopic if the test is passed.
     * 
     * @param userId
     * @param topicId
     * @param answers
     * @return TestResultDTO
     */
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

        int countPoint = 0;
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

            if (QuestionType.INPUT_TEXT.name().equals(question.getQuestionType())) {
                // For INPUT_TEXT questions, compare the user's input with the correct answer
                // Ignore case and trim whitespace
                String userInput = selectedAnswerContents.isEmpty() ? "" : selectedAnswerContents.get(0).trim().toLowerCase();
                String correctAnswer = correctAnswerContents.isEmpty() ? "" : correctAnswerContents.get(0).trim().toLowerCase();
                if (userInput.equals(correctAnswer)) {
                    countPoint += question.getPoint();
                }
            } else {
                // For CHOICE_ONE and CHOICE_MANY questions, use the existing logic
                if (new HashSet<>(selectedAnswerContents).containsAll(correctAnswerContents)
                        && new HashSet<>(correctAnswerContents).containsAll(selectedAnswerContents)) {
                    countPoint += question.getPoint();
                }
            }
        }

        int passPoint = topicDTO.getPassPoint();
        boolean isPassed = countPoint >= passPoint;
        double mark = Math.round(((double)countPoint / topicDTO.getMaxPoint()) * 100);

        // update LearnedTopic
        if (isPassed) {
            if(!learnedTopicRepository.existsByUserIdAndTopicId(userId,topicId)) {
                LearnedTopic learnedTopic = new LearnedTopic();
                learnedTopic.setUserId(userId);
                learnedTopic.setTopicId(topicId);
                learnedTopicRepository.save(learnedTopic);
            }
        }

        // save test result
        TestResult testResult = testResultRepository.findByUserIdAndTopicId(userId, topicId).orElse(null);
        if (testResult == null){
            testResult = new TestResult(mark, mark, isPassed, 1, topicType, user, topicEntity);
        } else {
            testResult.setLatestScore(mark);
            if (testResult.getParticipationCount() != null) {
                testResult.setParticipationCount(testResult.getParticipationCount()+1);
            } else {
                testResult.setParticipationCount(1);
            }
            if (testResult.getHighestScore() < mark) {
                testResult.setHighestScore(mark);
            }
            if (!testResult.getPassed()) {
                testResult.setPassed(isPassed);
            }
        }
        testResult = testResultRepository.save(testResult);
        return testResultMapper.toDTO(testResult);
    }
}
