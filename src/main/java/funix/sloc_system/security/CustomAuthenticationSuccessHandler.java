package funix.sloc_system.security;

import funix.sloc_system.entity.User;
import funix.sloc_system.repository.UserRepository;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();

        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null) {
            user.setFailedAttempts(0);
            userRepository.save(user);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
