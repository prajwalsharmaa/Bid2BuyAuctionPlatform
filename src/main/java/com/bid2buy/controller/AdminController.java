package com.bid2buy.controller;

import com.bid2buy.dto.CategoryDTO;
import com.bid2buy.entity.Auction;
import com.bid2buy.entity.Category;
import com.bid2buy.entity.User;
import com.bid2buy.repository.UserRepository;
import com.bid2buy.service.AuctionService;
import com.bid2buy.service.CategoryService;
import com.bid2buy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private AuctionService auctionService;
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Get all users and filter to show only those with completed profiles
        List<User> allUsers = userRepository.findAll();
        List<User> usersWithProfiles = allUsers.stream()
            .filter(user -> user.getUserProfile() != null)
            .toList();
        
        List<Category> categories = categoryService.getAllCategories();
        List<Auction> auctions = auctionService.getAllAuctions();
        
        model.addAttribute("users", usersWithProfiles);
        model.addAttribute("categories", categories);
        model.addAttribute("auctions", auctions);
        
        return "admin/dashboard";
    }
    
    @PostMapping("/user/{id}/approve")
    public String approveUser(@PathVariable("id") Long id) {
        userService.approveUser(id);
        return "redirect:/admin/dashboard";
    }
    
    @PostMapping("/user/{id}/reject")
    public String rejectUser(@PathVariable("id") Long id) {
        userService.rejectUser(id);
        return "redirect:/admin/dashboard";
    }
    
    @GetMapping("/category/create")
    public String showCreateCategoryForm(Model model) {
        model.addAttribute("categoryDTO", new CategoryDTO());
        return "admin/category-form";
    }
    
    @PostMapping("/category/create")
    public String createCategory(@Valid @ModelAttribute CategoryDTO categoryDTO,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            return "admin/category-form";
        }
        
        try {
            categoryService.createCategory(categoryDTO);
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/category-form";
        }
    }
    
    @GetMapping("/category/{id}/edit")
    public String showEditCategoryForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id);
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName(category.getName());
        categoryDTO.setDescription(category.getDescription());
        model.addAttribute("categoryDTO", categoryDTO);
        model.addAttribute("categoryId", id);
        return "admin/category-form";
    }
    
    @PostMapping("/category/{id}/edit")
    public String updateCategory(@PathVariable("id") Long id,
                                @Valid @ModelAttribute CategoryDTO categoryDTO,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categoryId", id);
            return "admin/category-form";
        }
        
        try {
            categoryService.updateCategory(id, categoryDTO);
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categoryId", id);
            return "admin/category-form";
        }
    }
    
    @PostMapping("/category/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/dashboard";
    }
    
    @PostMapping("/auction/{id}/disable")
    public String disableAuction(@PathVariable("id") Long id) {
        auctionService.disableAuction(id);
        return "redirect:/admin/dashboard";
    }
}

