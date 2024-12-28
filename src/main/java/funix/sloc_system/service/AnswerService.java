package funix.sloc_system.service;

import funix.sloc_system.entity.Answer;

import funix.sloc_system.repository.AnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerService {
    @Autowired
    private AnswerRepository answerRepository;

    public List<Answer> findByQuestionIdAndCorrectTrue(Long questionId) {
        return answerRepository.findByQuestionIdAndCorrectTrue(questionId);
    }
}
