package funix.sloc_system.service;

import funix.sloc_system.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private final String STUDENT_USER = "student_01";
    private final String STUDENT_EMAIL = "student1@example.com";
    private final Long STUDENT_ID = 4L;

    @Test
    public void testCheckRegistered_UsernameAndEmailNotEmpty() {
        String result = userService.checkRegistered("testuser", "test@example.com");
        assertEquals("Pass", result);
    }

    @Test
    public void testCheckRegistered_UsernameExists() {
        String result = userService.checkRegistered(STUDENT_USER, STUDENT_EMAIL);
        assertEquals("Username already exists", result);
    }

    @Test
    public void testCheckRegistered_EmailExists() {
        String result = userService.checkRegistered("newUser", STUDENT_EMAIL);
        assertEquals("Email already exists", result);
    }

    @Test
    public void testCheckRegistered_UsernameAndEmailEmpty() {
        String result = userService.checkRegistered("", "");
        assertEquals("Username and email cannot be empty", result);
    }

    @Test
    public void testFindByUsername_UserExists() {
        User user = userService.findByUsername(STUDENT_USER);
        assertNotNull(user);
        assertEquals(STUDENT_USER, user.getUsername());
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
        User user = userService.findByUsername(STUDENT_USER);
        user.setFullName("Updated Name");
        userService.updateUser(user);

        User updatedUser = userService.findByUsername(STUDENT_USER);
        assertEquals("Updated Name", updatedUser.getFullName());
    }

    @Test
    public void testGetCurrentUser() {
        // create authentication mock
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(STUDENT_USER, "studentpassword", AuthorityUtils.createAuthorityList("STUDENT"));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User currentUser = userService.getCurrentUser();
        assertNotNull(currentUser);
        assertEquals(STUDENT_USER, currentUser.getUsername());

        // clear context after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetCurrentUser_ThrowsExceptionError() {
        assertThrows(NullPointerException.class, () -> {
            userService.getCurrentUser();
        });
    }

    @Test
    public void testExistsByEmail() {
        assertTrue(userService.existsByEmail(STUDENT_EMAIL));
    }

    @Test
    public void testFindById() {
        User user = userService.findById(STUDENT_ID);
        assertNotNull(user);
        assertEquals(STUDENT_ID, user.getId());
    }

    @Test
    public void testGetUserById() {
        User user = userService.getUserById(STUDENT_ID);
        assertNotNull(user);
        assertEquals(STUDENT_ID, user.getId());
    }

    @Test
    public void testGetAllInstructors() {
        List<User> users = userService.getAllInstructors();
        assertNotNull(users);
        assertTrue(users.size() == 2);
    }

    @Test
    public void testFindByEmail() {
        User user = userService.findByEmail(STUDENT_EMAIL);
        assertNotNull(user);
        assertEquals(STUDENT_ID, user.getId());
    }

    @Test
    public void testExistsByUsername() {
        assertTrue(userService.existsByUsername(STUDENT_USER));
    }
}