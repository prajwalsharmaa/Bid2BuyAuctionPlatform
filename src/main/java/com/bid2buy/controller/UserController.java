package com.bid2buy.controller;

import com.bid2buy.dto.ProfileSetupDTO;
import com.bid2buy.entity.User;
import com.bid2buy.repository.UserRepository;
import com.bid2buy.service.AuctionService;
import com.bid2buy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Controller
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuctionService auctionService;
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = getCurrentUser();
        
        // Redirect to profile setup if profile is incomplete
        if (user.getUserProfile() == null) {
            return "redirect:/profile/setup";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("hasProfile", true);
        return "dashboard";
    }
    
    @GetMapping("/profile/setup")
    public String showProfileSetup(Model model) {
        User user = getCurrentUser();
        if (user.getUserProfile() != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("profileSetupDTO", new ProfileSetupDTO());
        return "profile-setup";
    }
    
    @PostMapping("/profile/setup")
    public String setupProfile(@Valid @ModelAttribute ProfileSetupDTO profileSetupDTO,
                              BindingResult result,
                              Model model) {
        if (result.hasErrors()) {
            return "profile-setup";
        }
        
        try {
            User user = getCurrentUser();
            userService.setupProfile(user, profileSetupDTO);
            return "redirect:/dashboard?profile=complete";
        } catch (IOException e) {
            model.addAttribute("error", "Error uploading file: " + e.getMessage());
            return "profile-setup";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "profile-setup";
        }
    }
    
    @PostMapping("/dashboard/toggle-buyer")
    public String toggleBuyerMode() {
        User user = getCurrentUser();
        userService.toggleBuyerMode(user);
        return "redirect:/dashboard";
    }
    
    @PostMapping("/dashboard/toggle-seller")
    public String toggleSellerMode() {
        User user = getCurrentUser();
        userService.toggleSellerMode(user);
        return "redirect:/dashboard";
    }
    
    @GetMapping("/auction/won")
    public String wonAuctions(Model model) {
        User user = getCurrentUser();
        model.addAttribute("wonAuctions", auctionService.getWonAuctionsByUser(user));
        return "won-auctions";
    }
}

