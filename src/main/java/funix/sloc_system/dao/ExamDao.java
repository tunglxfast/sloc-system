package funix.sloc_system.dao;

import funix.sloc_system.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamDao extends JpaRepository<Exam, Long> {
}
