package funix.sloc_system.repository;

import funix.sloc_system.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {

    Optional<TestResult> findByUserIdAndTopicId(Long userId, Long topicId);

}
