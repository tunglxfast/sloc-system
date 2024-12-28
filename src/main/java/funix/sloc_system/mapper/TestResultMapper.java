package funix.sloc_system.mapper;

import funix.sloc_system.dto.TestResultDTO;
import funix.sloc_system.entity.TestResult;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestResultMapper {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TopicMapper topicMapper;

    public TestResultDTO toDTO(TestResult testResult) {
        if (testResult == null) {
            return null;
        }

        TestResultDTO dto = new TestResultDTO();
        dto.setId(testResult.getId());
        dto.setHighestScore(testResult.getHighestScore());
        dto.setLatestScore(testResult.getLatestScore());
        dto.setPassed(testResult.getPassed());
        dto.setParticipationCount(testResult.getParticipationCount());
        dto.setTestType(testResult.getTestType());
        
        // Map related entities
        dto.setUser(userMapper.toDTO(testResult.getUser()));
        dto.setTopic(topicMapper.toDTO(testResult.getTopic()));
        
        return dto;
    }

    public TestResult toEntity(TestResultDTO dto, Topic topic, User user) {
        if (dto == null) {
            return null;
        }

        TestResult testResult = new TestResult();
        testResult.setId(dto.getId());
        testResult.setHighestScore(dto.getHighestScore());
        testResult.setLatestScore(dto.getLatestScore());
        testResult.setPassed(dto.getPassed());
        testResult.setParticipationCount(dto.getParticipationCount());
        testResult.setTestType(dto.getTestType());
        
        // Map related DTOs
        testResult.setUser(user);
        testResult.setTopic(topic);
        
        return testResult;
    }
} 
