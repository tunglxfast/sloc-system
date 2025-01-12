package funix.sloc_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import funix.sloc_system.entity.InstructorInfo;

public interface InstructorInfoRepository extends JpaRepository<InstructorInfo, Long> {

  Optional<InstructorInfo> findByUserId(Long userId);
}
