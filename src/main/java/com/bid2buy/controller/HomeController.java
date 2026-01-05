package com.bid2buy.controller;

import com.bid2buy.entity.Auction;
import com.bid2buy.entity.Category;
import com.bid2buy.repository.CategoryRepository;
import com.bid2buy.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {
    
    @Autowired
    private AuctionService auctionService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @GetMapping("/")
    public String home(@RequestParam(value = "categoryId", required = false) Long categoryId,
                       @RequestParam(value = "search", required = false) String search,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        
        // If search or category filter is applied, show filtered results
        if (categoryId != null || (search != null && !search.isEmpty())) {
            List<Auction> auctions;
            if (categoryId != null && search != null && !search.isEmpty()) {
                auctions = auctionService.searchAuctionsByCategoryAndKeyword(categoryId, search);
            } else if (categoryId != null) {
                auctions = auctionService.getAuctionsByCategory(categoryId);
            } else {
                auctions = auctionService.searchAuctions(search);
            }
            model.addAttribute("auctions", auctions);
        } else {
            // Show separate sections for Live and Upcoming auctions
            List<Auction> liveAuctions = auctionService.getLiveAuctions();
            List<Auction> upcomingAuctions = auctionService.getUpcomingAuctions();
            model.addAttribute("liveAuctions", liveAuctions);
            model.addAttribute("upcomingAuctions", upcomingAuctions);
        }
        
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("searchQuery", search);
        
        if (logout != null) {
            model.addAttribute("success", "You have been successfully logged out.");
        }
        
        return "index";
    }
}

