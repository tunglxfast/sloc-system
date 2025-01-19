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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class StudyProcessServiceTest {

    @Autowired
    private StudyProcessService studyProcessService;

    @Autowired
    private StudyProcessRepository studyProcessRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private LearnedTopicRepository learnedTopicRepository;

    @Autowired
    private AppUtil appUtil;

    @Test
    public void testFindByUserIdAndCourseId() {
        Long userId = 1L;
        Long courseId = 1L;
        StudyProcess studyProcess = studyProcessService.findByUserIdAndCourseId(userId, courseId);
        assertNotNull(studyProcess);
    }

    @Test
    public void testSaveLastViewTopic() {
        Long userId = 1L;
        Long courseId = 1L;
        Long topicId = 1L;
        StudyProcess studyProcess = studyProcessService.saveLastViewTopic(userId, courseId, topicId);
        assertNotNull(studyProcess);
        assertEquals(topicId, studyProcess.getLastViewTopic());
    }

    @Test
    public void testCalculateAndSaveFinalResult() {
        Long userId = 1L;
        Long courseId = 1L;
        Long topicId = 1L;
        String result = studyProcessService.calculateAndSaveFinalResult(userId, courseId, topicId);
        assertEquals("User didn't learning anything yet.", result);
    }
} 