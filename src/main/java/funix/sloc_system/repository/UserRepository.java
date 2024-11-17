package funix.sloc_system.repository;

import funix.sloc_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO của user chung
 */
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
