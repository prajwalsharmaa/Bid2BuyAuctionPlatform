package com.bid2buy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

public class ProfileSetupDTO {
    
    private MultipartFile profilePicture;
    
    private MultipartFile citizenshipCard;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    @NotBlank(message = "Phone number is required")
    private String phone;
    
    private String address;
    
    // Getters and Setters
    public MultipartFile getProfilePicture() {
        return profilePicture;
    }
    
    public void setProfilePicture(MultipartFile profilePicture) {
        this.profilePicture = profilePicture;
    }
    
    public MultipartFile getCitizenshipCard() {
        return citizenshipCard;
    }
    
    public void setCitizenshipCard(MultipartFile citizenshipCard) {
        this.citizenshipCard = citizenshipCard;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
}

