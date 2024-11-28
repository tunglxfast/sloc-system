package funix.sloc_system.dao;

import funix.sloc_system.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerDao extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionIdAndIsCorrectTrue(Long questionId);
}
