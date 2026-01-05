package com.bid2buy.dto;

import com.bid2buy.entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AddressDTO {
    
    private Long id;
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Address is required")
    private String addressLine;
    
    private String city;
    
    private String state;
    
    private String postalCode;
    
    private String country;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phoneNumber;
    
    private Address.AddressType addressType = Address.AddressType.HOME;
    
    private Boolean isDefaultShipping = false;
    
    private Boolean isDefaultBilling = false;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getAddressLine() {
        return addressLine;
    }
    
    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Address.AddressType getAddressType() {
        return addressType;
    }
    
    public void setAddressType(Address.AddressType addressType) {
        this.addressType = addressType;
    }
    
    public Boolean getIsDefaultShipping() {
        return isDefaultShipping;
    }
    
    public void setIsDefaultShipping(Boolean isDefaultShipping) {
        this.isDefaultShipping = isDefaultShipping;
    }
    
    public Boolean getIsDefaultBilling() {
        return isDefaultBilling;
    }
    
    public void setIsDefaultBilling(Boolean isDefaultBilling) {
        this.isDefaultBilling = isDefaultBilling;
    }
}

