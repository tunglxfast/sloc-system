package funix.sloc_system.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import funix.sloc_system.entity.Role;
import funix.sloc_system.repository.RoleRepository;

@Service
public class RoleService {
  @Autowired
  private RoleRepository roleRepository;

  public Set<Role> getRolesByNames(List<String> names) {
    Set<Role> roles = new HashSet<>();
    for (String name : names) {
      Role role = roleRepository.findByNameIgnoreCase(name).orElse(null);
      if (role != null) {
        roles.add(role);
      }
    }
    return roles;
  }

  public Set<Role> getAllRoles() {
    return new HashSet<>(roleRepository.findAll());
  }
}

