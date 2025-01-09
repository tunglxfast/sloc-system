package funix.sloc_system.service;

import funix.sloc_system.dto.TopicDiscussionDTO;
import funix.sloc_system.entity.Topic;
import funix.sloc_system.entity.TopicDiscussion;
import funix.sloc_system.entity.User;
import funix.sloc_system.mapper.TopicDiscussionMapper;
import funix.sloc_system.repository.TopicDiscussionRepository;
import funix.sloc_system.repository.TopicRepository;
import funix.sloc_system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopicDiscussionService {

    @Autowired
    private TopicDiscussionRepository topicDiscussionRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicDiscussionMapper topicDiscussionMapper;

    public List<TopicDiscussionDTO> getDiscussionsByTopicId(Long topicId) {
        List<TopicDiscussion> discussions = topicDiscussionRepository.findByTopicId(topicId);
        return discussions.stream()
                .map(topicDiscussionMapper::toDTO)
                .sorted(Comparator.comparing(TopicDiscussionDTO::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    public TopicDiscussionDTO getDiscussionById(Long id) {
        TopicDiscussion discussion = topicDiscussionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Discussion not found"));
        return topicDiscussionMapper.toDTO(discussion);
    }

    @Transactional
    public TopicDiscussionDTO createDiscussion(Long topicId, Long userId, String title, String content) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        TopicDiscussion discussion = new TopicDiscussion();
        discussion.setTitle(title);
        discussion.setContent(content);
        discussion.setTopic(topic);
        discussion.setCreatedBy(user);
        discussion.setCreatedAt(LocalDateTime.now());
        discussion.setUpdatedAt(LocalDateTime.now());

        topic.addDiscussion(discussion);
        TopicDiscussion savedDiscussion = topicDiscussionRepository.save(discussion);
        return topicDiscussionMapper.toDTO(savedDiscussion);
    }

    @Transactional
    public TopicDiscussionDTO updateDiscussion(Long id, String title, String content) {
        TopicDiscussion discussion = topicDiscussionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Discussion not found"));

        discussion.setTitle(title);
        discussion.setContent(content);
        discussion.setUpdatedAt(LocalDateTime.now());

        TopicDiscussion savedDiscussion = topicDiscussionRepository.save(discussion);
        return topicDiscussionMapper.toDTO(savedDiscussion);
    }

    @Transactional
    public void deleteDiscussion(Long id) {
        TopicDiscussion discussion = topicDiscussionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Discussion not found"));
        topicDiscussionRepository.delete(discussion);
    }

    public TopicDiscussion getTopicDiscussionEntity(Long id) {
        return topicDiscussionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Discussion not found"));
    }

    public List<TopicDiscussionDTO> getDiscussionsByCourseId(Long courseId) {
        List<TopicDiscussion> discussions = topicDiscussionRepository.findByCourseIdOrderByUpdatedAtDesc(courseId);
        return topicDiscussionMapper.toDTO(discussions);
    }
} 
