package com.bid2buy.service;

import com.bid2buy.dto.ProfileSetupDTO;
import com.bid2buy.dto.RegisterDTO;
import com.bid2buy.entity.User;
import com.bid2buy.entity.UserProfile;
import com.bid2buy.repository.UserProfileRepository;
import com.bid2buy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired(required = false)
    private SessionManagementService sessionManagementService;
    
    public User registerUser(RegisterDTO registerDTO) {
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setFirstName(registerDTO.getFirstName());
        user.setLastName(registerDTO.getLastName());
        user.setGender(registerDTO.getGender());
        user.setRole(User.Role.USER);
        user.setApproved(false);
        
        return userRepository.save(user);
    }
    
    public UserProfile setupProfile(User user, ProfileSetupDTO profileSetupDTO) throws IOException {
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
            .orElse(new UserProfile());
        
        profile.setUser(user);
        
        if (profileSetupDTO.getProfilePicture() != null && 
            !profileSetupDTO.getProfilePicture().isEmpty()) {
            String filename = fileStorageService.storeProfilePicture(
                profileSetupDTO.getProfilePicture());
            profile.setProfilePicture(filename);
        }
        
        if (profileSetupDTO.getCitizenshipCard() != null && 
            !profileSetupDTO.getCitizenshipCard().isEmpty()) {
            String filename = fileStorageService.storeCitizenshipCard(
                profileSetupDTO.getCitizenshipCard());
            profile.setCitizenshipCard(filename);
        }
        
        profile.setPhone(profileSetupDTO.getPhone());
        profile.setAddress(profileSetupDTO.getAddress());
        
        UserProfile savedProfile = userProfileRepository.save(profile);
        user.setUserProfile(savedProfile);
        userRepository.save(user);
        
        return savedProfile;
    }
    
    public void toggleBuyerMode(User user) {
        user.setBuyerMode(!user.getBuyerMode());
        if (user.getBuyerMode()) {
            user.setSellerMode(false);
        }
        userRepository.save(user);
    }
    
    public void toggleSellerMode(User user) {
        user.setSellerMode(!user.getSellerMode());
        if (user.getSellerMode()) {
            user.setBuyerMode(false);
        }
        userRepository.save(user);
    }
    
    public void approveUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setApproved(true);
        userRepository.save(user);
        
        // Invalidate user's session to force re-authentication with new authorities
        if (sessionManagementService != null) {
            sessionManagementService.invalidateUserSessions(user);
        }
    }
    
    public void rejectUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setApproved(false);
        userRepository.save(user);
        
        // Invalidate user's session
        if (sessionManagementService != null) {
            sessionManagementService.invalidateUserSessions(user);
        }
    }
}

