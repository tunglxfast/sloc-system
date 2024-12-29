package funix.sloc_system.service;

import funix.sloc_system.dto.CourseDTO;
import funix.sloc_system.entity.Course;
import funix.sloc_system.enums.ContentStatus;
import funix.sloc_system.repository.CourseRepository;
import funix.sloc_system.mapper.CourseMapper;
import funix.sloc_system.dto.ChapterDTO;
import funix.sloc_system.dto.TopicDTO;
import funix.sloc_system.dto.QuestionDTO;
import funix.sloc_system.dto.AnswerDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired; 

@Service
public class DTOService {
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseMapper courseMapper;

    public CourseDTO getAvailableCourseDTO(Long courseId) {
        Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        CourseDTO courseDTO = courseMapper.toDTO(course);
        courseDTO.setChapters(getAvailableChapters(courseDTO));
        return courseDTO;
    }

    public List<ChapterDTO> getAvailableChapters(CourseDTO courseDTO) {
        List<ChapterDTO> chapters = new ArrayList<>();
        String chapterContentStatus;
        for (ChapterDTO chapterDTO : courseDTO.getChapters()) {
            chapterContentStatus = chapterDTO.getContentStatus();
            if (chapterContentStatus.equals(ContentStatus.PUBLISHED.name()) 
                || chapterContentStatus.equals(ContentStatus.PUBLISHED_EDITING.name())) {
                chapterDTO.setTopics(getAvailableTopics(chapterDTO));
                chapters.add(chapterDTO);
            }
            
        } 
        return chapters;
    }

    public List<TopicDTO> getAvailableTopics(ChapterDTO chapterDTO) {
        List<TopicDTO> topics = new ArrayList<>();
        for (TopicDTO topicDTO : chapterDTO.getTopics()) {
            if (topicDTO.getContentStatus().equals(ContentStatus.PUBLISHED.name()) 
                || topicDTO.getContentStatus().equals(ContentStatus.PUBLISHED_EDITING.name())) {
                topicDTO.setQuestions(getAvailableQuestions(topicDTO));
                topics.add(topicDTO);
            }
        }
        return topics;
    }   

    public List<QuestionDTO> getAvailableQuestions(TopicDTO topicDTO) {
        List<QuestionDTO> questions = new ArrayList<>();
        for (QuestionDTO questionDTO : topicDTO.getQuestions()) {
            if (questionDTO.getContentStatus().equals(ContentStatus.PUBLISHED.name()) 
                || questionDTO.getContentStatus().equals(ContentStatus.PUBLISHED_EDITING.name())) { 
                questionDTO.setAnswers(getAvailableAnswers(questionDTO));
                questions.add(questionDTO);
            }
        }
        return questions;
    }

    public List<AnswerDTO> getAvailableAnswers(QuestionDTO questionDTO) {
        List<AnswerDTO> answers = new ArrayList<>();
        for (AnswerDTO answerDTO : questionDTO.getAnswers()) {
            if (answerDTO.getContentStatus().equals(ContentStatus.PUBLISHED.name()) 
                || answerDTO.getContentStatus().equals(ContentStatus.PUBLISHED_EDITING.name())) {
                answers.add(answerDTO);
            }
        }
        return answers;
    }
}

