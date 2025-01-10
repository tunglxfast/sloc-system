package funix.sloc_system.service;

import funix.sloc_system.dto.TestResultDTO;
import funix.sloc_system.entity.LearnedTopic;
import funix.sloc_system.entity.StudyProcess;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.repository.LearnedTopicRepository;
import funix.sloc_system.repository.StudyProcessRepository;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.util.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class StudyProcessService {
    @Autowired
    private StudyProcessRepository studyProcessRepository;
    @Autowired
    private AppUtil appUtil;
    @Autowired
    private LearnedTopicRepository learnedTopicRepository;
    @Autowired
    private TopicRepository topicRepository;

    public StudyProcess findByUserIdAndCourseId(Long userId, Long courseId) throws IllegalArgumentException {
        return studyProcessRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElse(new StudyProcess());
    }

    /*
     * Save the topic that the student recently viewed.
     * If the topic is not an exam or quiz, then add it to LearnedTopic.
     * @param userId
     * @param courseId
     * @param topicId
     * @return StudyProcess
     */
    @Transactional
    public StudyProcess saveLastViewTopic(Long userId, Long courseId, Long topicId){
        if (userId == null || courseId == null || topicId == null) {
            return null;
        }

        StudyProcess studyProcess = studyProcessRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElse(new StudyProcess());
        if (studyProcess.getUserId() == null) {
            studyProcess.setUserId(userId);
        }
        if (studyProcess.getCourseId() == null) {
            studyProcess.setCourseId(courseId);
        }

        double learningPercent = appUtil.calculateLearningProgress(userId, courseId);
        studyProcess.setLearningProgress(learningPercent);

        studyProcess.setLastViewTopic(topicId);

        // check and add topic to LearnedTopic
        Topic topic = topicRepository.findById(topicId).orElse(null);
        if (topic != null && topic.getTopicType() != TopicType.EXAM && topic.getTopicType() != TopicType.QUIZ) {
            if(!learnedTopicRepository.existsByUserIdAndTopicId(userId,topicId)) {
                LearnedTopic learnedTopic = new LearnedTopic();
                learnedTopic.setUserId(userId);
                learnedTopic.setTopicId(topicId);
                learnedTopicRepository.save(learnedTopic);
            }
        }

        return studyProcessRepository.save(studyProcess);
    }

    @Transactional
    public String calculateAndSaveFinalResult(Long userId, Long courseId, Long topicId) {
        List<TestResultDTO> testResults = appUtil.getCourseTestsResult(userId, courseId);
        StudyProcess studyProcess = studyProcessRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);

        if (studyProcess == null || testResults.isEmpty()) {
            // must have studyProcess history before have final result
            return "User didn't learning anything yet.";
        }

        boolean isCalculateFinal = true;
        for (TestResultDTO testResultDTO: testResults) {
            if (testResultDTO.getHighestScore() == null) {
                isCalculateFinal = false;
                break;
            }
        }

        if (isCalculateFinal) {
            Map<String, Object> finalCourseResult = appUtil.calculateFinalScore(testResults);
            int finalScore = (int) finalCourseResult.get("finalScore");
            boolean passed = (boolean) finalCourseResult.get("isPassed");
            studyProcess.setFinalScore(finalScore);
            studyProcess.setPassCourse(passed);
            studyProcessRepository.save(studyProcess);
            return "Calculate and save score successfully.";
        } else {
            return "Not qualify to calculate final score yet.";
        }
    }

}
