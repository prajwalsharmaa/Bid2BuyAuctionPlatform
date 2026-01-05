package com.bid2buy.controller;

import com.bid2buy.dto.AuctionCreateDTO;
import com.bid2buy.dto.AuctionWinnerDTO;
import com.bid2buy.entity.Address;
import com.bid2buy.entity.Auction;
import com.bid2buy.entity.Category;
import com.bid2buy.entity.User;
import com.bid2buy.repository.CategoryRepository;
import com.bid2buy.repository.UserRepository;
import com.bid2buy.service.AddressService;
import com.bid2buy.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuctionController {
    
    @Autowired
    private AuctionService auctionService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AddressService addressService;
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @GetMapping("/auction/create")
    public String showCreateAuctionForm(Model model) {
        User user = getCurrentUser();
        if (!user.getApproved() || !user.getSellerMode()) {
            return "redirect:/dashboard?error=not_approved_seller";
        }
        
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("auctionCreateDTO", new AuctionCreateDTO());
        return "auction-create";
    }
    
    @PostMapping("/auction/create")
    public String createAuction(@Valid @ModelAttribute AuctionCreateDTO auctionCreateDTO,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            return "auction-create";
        }
        
        try {
            User seller = getCurrentUser();
            auctionService.createAuction(seller, auctionCreateDTO);
            return "redirect:/dashboard?auction=created";
        } catch (IllegalStateException e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            return "auction-create";
        } catch (IOException e) {
            model.addAttribute("error", "Error uploading image: " + e.getMessage());
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            return "auction-create";
        }
    }
    
    @GetMapping("/auction/{id}")
    public String viewAuction(@PathVariable("id") Long id, Model model) {
        Auction auction = auctionService.getAuctionById(id);
        model.addAttribute("auction", auction);
        
        User currentUser = null;
        boolean canBid = false;
        try {
            currentUser = getCurrentUser();
            model.addAttribute("currentUser", currentUser);
            canBid = currentUser.getApproved() && 
                     currentUser.getBuyerMode() && 
                     !auction.getSeller().getId().equals(currentUser.getId()) &&
                     auction.getStatus() == Auction.AuctionStatus.LIVE;
            
            // If seller viewing their own ended auction, show winner info
            if (auction.getSeller().getId().equals(currentUser.getId()) && 
                auction.getStatus() == Auction.AuctionStatus.ENDED) {
                User winner = auctionService.getAuctionWinner(auction);
                Address deliveryAddress = null;
                
                if (winner != null) {
                    List<Address> addresses = addressService.getAddressesByUser(winner);
                    // Get default shipping address, or first address if no default
                    deliveryAddress = addresses.stream()
                        .filter(addr -> addr.getIsDefaultShipping() != null && addr.getIsDefaultShipping())
                        .findFirst()
                        .orElse(addresses.isEmpty() ? null : addresses.get(0));
                }
                
                model.addAttribute("winnerInfo", new AuctionWinnerDTO(winner, deliveryAddress));
            }
        } catch (Exception e) {
            // User not logged in
        }
        model.addAttribute("canBid", canBid);
        
        return "auction-detail";
    }
    
    @GetMapping("/auction/my-auctions")
    public String myAuctions(Model model) {
        User user = getCurrentUser();
        List<Auction> auctions = auctionService.getAuctionsBySeller(user);
        
        // Prepare winner information for ended auctions
        Map<Long, AuctionWinnerDTO> winnerInfoMap = new HashMap<>();
        for (Auction auction : auctions) {
            if (auction.getStatus() == Auction.AuctionStatus.ENDED) {
                User winner = auctionService.getAuctionWinner(auction);
                Address deliveryAddress = null;
                
                if (winner != null) {
                    List<Address> addresses = addressService.getAddressesByUser(winner);
                    // Get default shipping address, or first address if no default
                    deliveryAddress = addresses.stream()
                        .filter(addr -> addr.getIsDefaultShipping() != null && addr.getIsDefaultShipping())
                        .findFirst()
                        .orElse(addresses.isEmpty() ? null : addresses.get(0));
                }
                
                winnerInfoMap.put(auction.getId(), new AuctionWinnerDTO(winner, deliveryAddress));
            }
        }
        
        model.addAttribute("auctions", auctions);
        model.addAttribute("winnerInfoMap", winnerInfoMap);
        return "my-auctions";
    }
}

