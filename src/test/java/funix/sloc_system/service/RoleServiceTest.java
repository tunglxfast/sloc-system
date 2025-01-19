package funix.sloc_system.service;

import funix.sloc_system.entity.Role;
import funix.sloc_system.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testGetRolesByNames() {
        Set<Role> roles = roleService.getRolesByNames(List.of("STUDENT", "ADMIN"));
        assertNotNull(roles);
        assertEquals(2, roles.size());
    }

    @Test
    public void testGetAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertTrue(roles.size() == 4);
    }
} 