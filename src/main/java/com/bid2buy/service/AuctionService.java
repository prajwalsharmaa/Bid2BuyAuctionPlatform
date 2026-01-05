package com.bid2buy.service;

import com.bid2buy.dto.AuctionCreateDTO;
import com.bid2buy.entity.Auction;
import com.bid2buy.entity.Auction.AuctionStatus;
import com.bid2buy.entity.Bid;
import com.bid2buy.entity.Category;
import com.bid2buy.entity.User;
import com.bid2buy.repository.AuctionRepository;
import com.bid2buy.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuctionService {
    
    @Autowired
    private AuctionRepository auctionRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll().stream()
            .filter(auction -> auction.getStatus() != AuctionStatus.ENDED)
            .collect(Collectors.toList());
    }
    
    public List<Auction> getLiveAuctions() {
        return auctionRepository.findByStatus(AuctionStatus.LIVE);
    }
    
    public List<Auction> getUpcomingAuctions() {
        return auctionRepository.findByStatus(AuctionStatus.UPCOMING);
    }
    
    public List<Auction> getAuctionsByCategory(Long categoryId) {
        List<Auction> auctions = auctionRepository.findByCategoryId(categoryId);
        return auctions.stream()
            .filter(auction -> auction.getStatus() != AuctionStatus.ENDED)
            .collect(Collectors.toList());
    }
    
    public List<Auction> searchAuctions(String keyword) {
        return auctionRepository.searchByProductName(keyword);
    }
    
    public List<Auction> searchAuctionsByCategoryAndKeyword(Long categoryId, String keyword) {
        return auctionRepository.searchByCategoryAndProductName(categoryId, keyword);
    }
    
    public Auction getAuctionById(Long id) {
        return auctionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Auction not found"));
    }
    
    public Auction createAuction(User seller, AuctionCreateDTO auctionDTO) throws IOException {
        if (!seller.getApproved() || !seller.getSellerMode()) {
            throw new IllegalStateException("User must be approved and in seller mode to create auctions");
        }
        
        Category category = categoryRepository.findById(auctionDTO.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        if (auctionDTO.getEndTime().isBefore(auctionDTO.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        if (auctionDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time must be in the future");
        }
        
        Auction auction = new Auction();
        auction.setSeller(seller);
        auction.setProductName(auctionDTO.getProductName());
        auction.setDescription(auctionDTO.getDescription());
        auction.setCategory(category);
        auction.setBasePrice(auctionDTO.getBasePrice());
        auction.setStartTime(auctionDTO.getStartTime());
        auction.setEndTime(auctionDTO.getEndTime());
        
        if (auctionDTO.getImage() != null && !auctionDTO.getImage().isEmpty()) {
            String filename = fileStorageService.storeAuctionImage(auctionDTO.getImage());
            auction.setImage(filename);
        }
        
        // Set initial status
        if (auction.getStartTime().isAfter(LocalDateTime.now())) {
            auction.setStatus(AuctionStatus.UPCOMING);
        } else {
            auction.setStatus(AuctionStatus.LIVE);
        }
        
        return auctionRepository.save(auction);
    }
    
    public List<Auction> getAuctionsBySeller(User seller) {
        return auctionRepository.findBySellerId(seller.getId());
    }
    
    @Scheduled(fixedRate = 60000) // Run every minute
    public void updateAuctionStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> auctions = auctionRepository.findAll();
        
        for (Auction auction : auctions) {
            if (auction.getStatus() == AuctionStatus.UPCOMING && 
                !auction.getStartTime().isAfter(now)) {
                auction.setStatus(AuctionStatus.LIVE);
                auctionRepository.save(auction);
            }
            
            if (auction.getStatus() == AuctionStatus.LIVE && 
                !auction.getEndTime().isAfter(now)) {
                auction.setStatus(AuctionStatus.ENDED);
                auctionRepository.save(auction);
            }
        }
    }
    
    public void disableAuction(Long id) {
        Auction auction = getAuctionById(id);
        auction.setStatus(AuctionStatus.ENDED);
        auctionRepository.save(auction);
    }
    
    /**
     * Get auctions won by a user (ended auctions where user has the highest bid)
     */
    public List<Auction> getWonAuctionsByUser(User user) {
        List<Auction> endedAuctions = auctionRepository.findByStatus(AuctionStatus.ENDED);
        
        return endedAuctions.stream()
            .filter(auction -> {
                Bid highestBid = auction.getHighestBid();
                return highestBid != null && highestBid.getBidder().getId().equals(user.getId());
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get the winner of an auction (user with highest bid)
     */
    public User getAuctionWinner(Auction auction) {
        if (auction.getStatus() != AuctionStatus.ENDED) {
            return null;
        }
        Bid highestBid = auction.getHighestBid();
        return highestBid != null ? highestBid.getBidder() : null;
    }
}

