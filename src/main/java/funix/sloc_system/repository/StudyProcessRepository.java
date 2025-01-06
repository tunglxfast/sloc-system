package funix.sloc_system.repository;

import funix.sloc_system.entity.StudyProcess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudyProcessRepository extends JpaRepository<StudyProcess, Long> {
    Optional<StudyProcess> findByUserIdAndCourseId(Long userId, Long courseId);
}
