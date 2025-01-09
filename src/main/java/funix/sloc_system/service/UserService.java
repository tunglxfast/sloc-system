package funix.sloc_system.service;

import funix.sloc_system.entity.User;
import funix.sloc_system.enums.RoleType;
import funix.sloc_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Chứa các chức năng của user
 * làm việc với Repository
 */
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public String checkRegistered(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Username already exists";
        } else if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already exists";
        } else {
            return "Pass";
        }
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<User> getAllInstructors() {
        return userRepository.findAllInstructors();
    }
}
