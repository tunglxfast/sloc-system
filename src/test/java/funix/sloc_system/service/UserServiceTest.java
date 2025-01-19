package funix.sloc_system.service;

import funix.sloc_system.entity.User;
import funix.sloc_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testCheckRegistered_UsernameAndEmailNotEmpty() {
        String result = userService.checkRegistered("testuser", "test@example.com");
        assertEquals("Pass", result);
    }

    @Test
    public void testCheckRegistered_UsernameExists() {
        String result = userService.checkRegistered("student_01", "student1@example.com");
        assertEquals("Username already exists", result);
    }

    @Test
    public void testCheckRegistered_EmailExists() {
        String result = userService.checkRegistered("newUser", "student1@example.com");
        assertEquals("Email already exists", result);
    }

    @Test
    public void testCheckRegistered_UsernameAndEmailEmpty() {
        String result = userService.checkRegistered("", "");
        assertEquals("Username and email cannot be empty", result);
    }

    @Test
    public void testFindByUsername_UserExists() {
        User user = userService.findByUsername("student_01");
        assertNotNull(user);
        assertEquals("student_01", user.getUsername());
    }

    @Test
    public void testFindByUsername_UserNotExists() {
        User user = userService.findByUsername("nonExistingUser");
        assertNull(user);
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
        assertTrue(users.size() == 14);
    }

    @Test
    public void testUpdateUser() {
        User user = userService.findByUsername("student_01");
        user.setFullName("Updated Name");
        userService.updateUser(user);

        User updatedUser = userService.findByUsername("student_01");
        assertEquals("Updated Name", updatedUser.getFullName());
    }

    @Test
    public void testGetCurrentUser() {
        // create authentication mock
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("student_01", "studentpassword", AuthorityUtils.createAuthorityList("STUDENT"));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User currentUser = userService.getCurrentUser();
        assertNotNull(currentUser);
        assertEquals("student_01", currentUser.getUsername());

        // clear context after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetCurrentUser_ThrowsExceptionError() {
        assertThrows(NullPointerException.class, () -> {
            userService.getCurrentUser();
        });
    }
}