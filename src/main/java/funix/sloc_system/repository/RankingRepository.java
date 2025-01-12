package funix.sloc_system.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import funix.sloc_system.entity.Ranking;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {
    List<Ranking> findByCourseIdOrderByAllCourseScoreDesc(Long courseId);

    Optional<Ranking> findByUserIdAndCourseId(Long userId, Long courseId);

    List<Ranking> findByUserId(Long userId);
}

