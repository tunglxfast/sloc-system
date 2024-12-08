package funix.sloc_system.service;

import funix.sloc_system.dao.UserDao;
import funix.sloc_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Chứa các chức năng của user
 * làm việc với Repository
 */
@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    public String checkRegistered(User user) {
        if (userDao.existsByUsername(user.getUsername())) {
            return "Username already exists";
        } else if (userDao.existsByEmail(user.getEmail())) {
            return "Email already exists";
        } else {
            return "Pass";
        }
    }

    public User findByUsername(String username) {
        return userDao.findByUsername(username).orElse(null);
    }

    public boolean existsByUsername(String username) {
        return userDao.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    public User findById(Long id) {
        return userDao.findById(id).orElse(null);
    }
}
