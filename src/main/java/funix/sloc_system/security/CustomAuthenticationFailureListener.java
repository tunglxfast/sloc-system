package funix.sloc_system.security;

import funix.sloc_system.entity.User;
import funix.sloc_system.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationFailureListener extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage;
        if (exception instanceof LockedException) {
            errorMessage = "Your account is locked.";
        } else {
            String username = request.getParameter("username");

            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                int attempts = user.getFailedAttempts() + 1;
                user.setFailedAttempts(attempts);
    
                // Lock the account if the number of failed attempts is greater than or equal to 3
                if (attempts >= 3) {
                    user.setLocked(true); 
                }
    
                userRepository.save(user);
            }
            errorMessage = "Invalid username or password.";
            
        }
        // Redirect with error message as query parameter
        response.sendRedirect(request.getContextPath() + "/login?error=" + errorMessage);
    }
}
