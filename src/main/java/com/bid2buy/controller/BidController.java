package com.bid2buy.controller;

import com.bid2buy.dto.BidDTO;
import com.bid2buy.dto.BidResponseDTO;
import com.bid2buy.entity.Bid;
import com.bid2buy.entity.User;
import com.bid2buy.repository.UserRepository;
import com.bid2buy.service.BidService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class BidController {
    
    @Autowired
    private BidService bidService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @PostMapping("/bid/place")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> placeBid(@Valid @RequestBody BidDTO bidDTO,
                                                         BindingResult result) {
        Map<String, Object> response = new HashMap<>();
        
        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Invalid bid data");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            User bidder = getCurrentUser();
            Bid bid = bidService.placeBid(bidder, bidDTO);
            
            // Convert to DTO for WebSocket broadcast
            BidResponseDTO bidResponseDTO = bidService.convertToDTO(bid);
            
            // Broadcast to all subscribers
            messagingTemplate.convertAndSend("/topic/auction/" + bidDTO.getAuctionId(), bidResponseDTO);
            
            response.put("success", true);
            response.put("message", "Bid placed successfully");
            response.put("bid", bidResponseDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error placing bid: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/bid/my-bids")
    public String myBids(org.springframework.ui.Model model) {
        User user = getCurrentUser();
        model.addAttribute("bids", bidService.getBidsByBidder(user));
        return "my-bids";
    }
}

