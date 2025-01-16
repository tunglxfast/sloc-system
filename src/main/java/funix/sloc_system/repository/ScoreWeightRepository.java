package funix.sloc_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import funix.sloc_system.entity.ScoreWeight;

public interface ScoreWeightRepository extends JpaRepository<ScoreWeight, Long> {
  Optional<ScoreWeight> findByCourseId(Long courseId);
  
}
