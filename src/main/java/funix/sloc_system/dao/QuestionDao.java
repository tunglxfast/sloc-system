package funix.sloc_system.dao;

import funix.sloc_system.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface QuestionDao extends JpaRepository<Question, Long> {
    List<Question> findByTopicId(Long TopicId);
}
