package funix.sloc_system.mapper;

import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.entity.Chapter;
import funix.sloc_system.entity.Question;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.enums.TopicType;
import funix.sloc_system.repository.ChapterRepository;
import funix.sloc_system.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TopicMapper {
    
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private TopicRepository topicRepository;


    public TopicDTO toDTO(Topic topic) {
        if (topic == null) {
            return null;
        }

        TopicDTO dto = new TopicDTO();
        dto.setId(topic.getId());
        dto.setTitle(topic.getTitle());
        dto.setDescription(topic.getDescription());
        dto.setTopicType(topic.getTopicType().toString());
        dto.setSequence(topic.getSequence());
        dto.setChapterId(topic.getChapter() != null ? topic.getChapter().getId() : null);
        dto.setContentStatus(topic.getContentStatus().toString());
        
        // Map type-specific fields
        switch (topic.getTopicType()) {
            case VIDEO:
                dto.setVideoUrl(topic.getVideoUrl());
                break;
            case READING:
                dto.setFileUrl(topic.getFileUrl());
                break;
            case QUIZ:
            case EXAM:
                dto.setPassScore(topic.getPassScore());
                dto.setTotalScore(topic.getTotalScore());
                if (topic.getTopicType() == TopicType.EXAM) {
                    dto.setTimeLimit(topic.getTimeLimit());
                }
                // Map questions
                if (topic.getQuestions() != null) {
                    for (Question question : topic.getQuestions()) {
                        QuestionDTO questionDTO = questionMapper.toDTO(question);
                        dto.addQuestion(questionDTO);
                    }
                }
                break;
        }
        
        return dto;
    }

    @Transactional
    public Topic toEntity(TopicDTO dto, Chapter chapter) {
        if (dto == null) {
            return null;
        }

        Topic topic;
        if (dto.getId() != null) {
            topic = topicRepository.findById(dto.getId()).orElse(new Topic());
        } else {
            topic = new Topic();
        }

        if (topic.getId() == null && dto.getId() != null) {
            topic.setId(dto.getId());
        }

        topic.setTitle(dto.getTitle());
        topic.setDescription(dto.getDescription());
        
        if (dto.getTopicType() != null) {
            topic.setTopicType(TopicType.valueOf(dto.getTopicType()));
        }
        
        topic.setSequence(dto.getSequence());

        topic.setChapter(chapter);

        if (dto.getContentStatus() != null) {
            topic.setContentStatus(ContentStatus.valueOf(dto.getContentStatus()));
        }

        topic.setFileUrl(dto.getFileUrl());
        topic.setVideoUrl(dto.getVideoUrl());
        topic.setPassScore(dto.getPassScore());
        topic.setTotalScore(dto.getTotalScore());
        topic.setTimeLimit(dto.getTimeLimit());

        // Map questions if present
        if (dto.getQuestions() != null) {
            if (topic.getQuestions() != null) {
                topic.getQuestions().clear();
            } else {
                topic.setQuestions(new ArrayList<>());
            }
            for (QuestionDTO questionDTO : dto.getQuestions()) {
                topic.addQuestion(questionMapper.toEntity(questionDTO, topic));
            }
        }
        
        return topic;
    }

    public List<TopicDTO> toDTO(List<Topic> topics) {
        return topics.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<Topic> toEntity(List<TopicDTO> topicDTOList, Chapter chapter) {
        return topicDTOList.stream().map(dto -> toEntity(dto, chapter)).collect(Collectors.toList());
    }
} 