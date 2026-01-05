package com.bid2buy.service;

import com.bid2buy.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionManagementService {
    
    @Autowired(required = false)
    private SessionRegistry sessionRegistry;
    
    public void invalidateUserSessions(User user) {
        if (sessionRegistry != null) {
            List<Object> principals = sessionRegistry.getAllPrincipals();
            for (Object principal : principals) {
                // Check if principal is UserDetails (from authentication)
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    org.springframework.security.core.userdetails.UserDetails userDetails = 
                        (org.springframework.security.core.userdetails.UserDetails) principal;
                    if (userDetails.getUsername().equals(user.getEmail())) {
                        sessionRegistry.getAllSessions(principal, false)
                            .forEach(sessionInformation -> sessionInformation.expireNow());
                    }
                }
                // Also check if principal is Authentication object
                else if (principal instanceof org.springframework.security.core.Authentication) {
                    org.springframework.security.core.Authentication auth = 
                        (org.springframework.security.core.Authentication) principal;
                    if (auth.getName().equals(user.getEmail())) {
                        sessionRegistry.getAllSessions(principal, false)
                            .forEach(sessionInformation -> sessionInformation.expireNow());
                    }
                }
            }
        }
    }
}

