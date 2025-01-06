package funix.sloc_system.repository;


import funix.sloc_system.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findByName(String name);

	Optional<Role> findByNameIgnoreCase(String name);
	
}
