package funix.sloc_system.service;

import funix.sloc_system.entity.User;
import funix.sloc_system.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Chứa các chức năng của user
 * làm việc với Repository
 */
@Service
public class UserService {
    @Autowired
    private UserDAO userDAO;

    public String checkRegistered(User user) {
        // Kiểm tra username hoặc email đã tồn tại
        if (userDAO.existsByUsername(user.getUsername())) {
            return "Username already exists";
        } else if (userDAO.existsByEmail(user.getEmail())) {
            return "Email already exists";
        } else {
            return "Pass";
        }
    }
}
