package funix.sloc_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import funix.sloc_system.entity.ForgotPassword;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Long> {
  Optional<ForgotPassword> findByEmail(String email);

  Optional<ForgotPassword> findByToken(String token);
}

