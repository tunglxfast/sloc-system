package funix.sloc_system.service;

import funix.sloc_system.entity.Question;
import funix.sloc_system.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    public List<Question> getQuestionsByTest(Long testId) {
        return questionRepository.findByTestId(testId);
    }
}

