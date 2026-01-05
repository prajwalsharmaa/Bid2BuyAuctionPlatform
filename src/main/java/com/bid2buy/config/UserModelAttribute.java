package com.bid2buy.config;

import com.bid2buy.entity.User;
import com.bid2buy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UserModelAttribute {
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${admin.email}")
    private String adminEmail;
    
    @ModelAttribute
    public void addUserToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            
            // Don't try to load admin user from database
            if (!email.equals(adminEmail)) {
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    model.addAttribute("currentUser", user);
                }
            }
        }
    }
}

