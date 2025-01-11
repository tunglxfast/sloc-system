package funix.sloc_system.repository;

import funix.sloc_system.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {

    Optional<TestResult> findByUserIdAndTopicId(Long userId, Long topicId);

    // Calculate total scores for each user in a course
    @Query("SELECT tr.user.id AS userId, tr.topic.chapter.course.id AS courseId, SUM(tr.highestScore) AS totalScore " +
           "FROM TestResult tr " +
           "WHERE tr.topic.chapter.course.id = :courseId " +
           "GROUP BY tr.user.id, tr.topic.chapter.course.id " +
           "ORDER BY totalScore DESC")
    List<Object[]> calculateTotalScores(@Param("courseId") Long courseId);

    @Query("SELECT tr.user.id AS userId, tr.topic.chapter.course.id AS courseId, " +
        "SUM(CASE WHEN tr.testType = 'QUIZ' THEN tr.highestScore * 0.4 " +
                    "WHEN tr.testType = 'EXAM' THEN tr.highestScore * 0.6 " +
                    "ELSE 0 END) AS totalScore " +
        "FROM TestResult tr " +
        "WHERE tr.topic.chapter.course.id = :courseId " +
        "AND tr.testType IN ('QUIZ', 'EXAM') " +
        "GROUP BY tr.user.id, tr.topic.chapter.course.id " +
        "ORDER BY totalScore DESC")
    List<Object[]> calculateWeightedTotalScores(@Param("courseId") Long courseId);

}
