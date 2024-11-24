package funix.sloc_system.dao;


import funix.sloc_system.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleDao extends JpaRepository<Role, Long> {

	Optional<Role> findByName(String name);
	
}
