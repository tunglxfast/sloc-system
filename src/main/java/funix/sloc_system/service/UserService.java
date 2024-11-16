package funix.sloc_system.service;

import funix.sloc_system.entity.User;
import funix.sloc_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public String checkRegistered(User user) {
        // Kiểm tra username hoặc email đã tồn tại
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Username already exists";
        } else if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already exists";
        } else {
            return "Pass";
        }
    }
}
