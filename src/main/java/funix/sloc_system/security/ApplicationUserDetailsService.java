package funix.sloc_system.security;

import funix.sloc_system.dao.RoleDao;
import funix.sloc_system.dao.UserDao;
import funix.sloc_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApplicationUserDetailsService implements UserDetailsService {
    private UserDao userDao;

    private RoleDao roleDao;

    @Autowired
    public ApplicationUserDetailsService(UserDao userDao, RoleDao roleDao) {
        this.userDao = userDao;
        this.roleDao = roleDao;
    }

    public User findByUsername(String username) {
        Optional<User> response = userDao.findByUsername(username);
        if (response.isEmpty()) {
            return null;
        } else {
            return response.get();
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userDao.findByUsername(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new SecurityUser(user.get());
    }
}
