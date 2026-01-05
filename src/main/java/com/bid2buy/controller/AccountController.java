package com.bid2buy.controller;

import com.bid2buy.dto.AddressDTO;
import com.bid2buy.dto.ProfileSetupDTO;
import com.bid2buy.entity.Address;
import com.bid2buy.entity.User;
import com.bid2buy.repository.UserRepository;
import com.bid2buy.service.AddressService;
import com.bid2buy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private UserRepository userRepository;
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @GetMapping("/manage")
    public String manageAccount(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        return "account/manage-account";
    }
    
    @GetMapping("/profile")
    public String editProfile(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        if (user.getUserProfile() != null) {
            ProfileSetupDTO profileDTO = new ProfileSetupDTO();
            profileDTO.setPhone(user.getUserProfile().getPhone());
            profileDTO.setAddress(user.getUserProfile().getAddress());
            model.addAttribute("profileDTO", profileDTO);
        } else {
            model.addAttribute("profileDTO", new ProfileSetupDTO());
        }
        return "account/edit-profile";
    }
    
    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute ProfileSetupDTO profileDTO,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            User user = getCurrentUser();
            model.addAttribute("user", user);
            return "account/edit-profile";
        }
        
        try {
            User user = getCurrentUser();
            userService.setupProfile(user, profileDTO);
            return "redirect:/account/profile?success=updated";
        } catch (IOException e) {
            model.addAttribute("error", "Error uploading file: " + e.getMessage());
            User user = getCurrentUser();
            model.addAttribute("user", user);
            return "account/edit-profile";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            User user = getCurrentUser();
            model.addAttribute("user", user);
            return "account/edit-profile";
        }
    }
    
    @GetMapping("/addresses")
    public String addressBook(Model model) {
        User user = getCurrentUser();
        List<Address> addresses = addressService.getAddressesByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        return "account/address-book";
    }
    
    @GetMapping("/addresses/new")
    public String showAddAddressForm(Model model) {
        model.addAttribute("addressDTO", new AddressDTO());
        return "account/address-form";
    }
    
    @PostMapping("/addresses/new")
    public String createAddress(@Valid @ModelAttribute AddressDTO addressDTO,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "account/address-form";
        }
        
        try {
            User user = getCurrentUser();
            addressService.createAddress(user, addressDTO);
            return "redirect:/account/addresses?success=created";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "account/address-form";
        }
    }
    
    @GetMapping("/addresses/{id}/edit")
    public String showEditAddressForm(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        Address address = addressService.getAddressById(id, user);
        
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setId(address.getId());
        addressDTO.setFullName(address.getFullName());
        addressDTO.setAddressLine(address.getAddressLine());
        addressDTO.setCity(address.getCity());
        addressDTO.setState(address.getState());
        addressDTO.setPostalCode(address.getPostalCode());
        addressDTO.setCountry(address.getCountry());
        addressDTO.setPhoneNumber(address.getPhoneNumber());
        addressDTO.setAddressType(address.getAddressType());
        addressDTO.setIsDefaultShipping(address.getIsDefaultShipping());
        addressDTO.setIsDefaultBilling(address.getIsDefaultBilling());
        
        model.addAttribute("addressDTO", addressDTO);
        model.addAttribute("addressId", id);
        return "account/address-form";
    }
    
    @PostMapping("/addresses/{id}/edit")
    public String updateAddress(@PathVariable Long id,
                               @Valid @ModelAttribute AddressDTO addressDTO,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("addressId", id);
            return "account/address-form";
        }
        
        try {
            User user = getCurrentUser();
            addressService.updateAddress(id, user, addressDTO);
            return "redirect:/account/addresses?success=updated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("addressId", id);
            return "account/address-form";
        }
    }
    
    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(@PathVariable Long id) {
        User user = getCurrentUser();
        addressService.deleteAddress(id, user);
        return "redirect:/account/addresses?success=deleted";
    }
    
    @PostMapping("/addresses/{id}/set-default-shipping")
    public String setDefaultShipping(@PathVariable Long id) {
        User user = getCurrentUser();
        addressService.setDefaultShipping(id, user);
        return "redirect:/account/addresses?success=default_shipping_set";
    }
    
    @PostMapping("/addresses/{id}/set-default-billing")
    public String setDefaultBilling(@PathVariable Long id) {
        User user = getCurrentUser();
        addressService.setDefaultBilling(id, user);
        return "redirect:/account/addresses?success=default_billing_set";
    }
}

