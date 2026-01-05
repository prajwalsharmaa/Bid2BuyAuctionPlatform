package com.bid2buy.config;

import com.bid2buy.entity.User;
import com.bid2buy.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${admin.email}")
    private String adminEmail;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        
        // Admin always goes to admin dashboard
        if (email.equals(adminEmail)) {
            response.sendRedirect("/admin/dashboard");
            return;
        }
        
        // Check if user has completed profile setup
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.getUserProfile() == null) {
            // Redirect to profile setup if profile is incomplete
            response.sendRedirect("/profile/setup");
        } else {
            // Otherwise go to dashboard
            response.sendRedirect("/dashboard");
        }
    }
}

