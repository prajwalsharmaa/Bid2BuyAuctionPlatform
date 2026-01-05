package com.bid2buy.service;

import com.bid2buy.dto.BidDTO;
import com.bid2buy.dto.BidResponseDTO;
import com.bid2buy.entity.Auction;
import com.bid2buy.entity.Bid;
import com.bid2buy.entity.User;
import com.bid2buy.repository.AuctionRepository;
import com.bid2buy.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BidService {
    
    @Autowired
    private BidRepository bidRepository;
    
    @Autowired
    private AuctionRepository auctionRepository;
    
    public Bid placeBid(User bidder, BidDTO bidDTO) {
        Auction auction = auctionRepository.findById(bidDTO.getAuctionId())
            .orElseThrow(() -> new IllegalArgumentException("Auction not found"));
        
        // Validation checks
        if (!bidder.getApproved() || !bidder.getBuyerMode()) {
            throw new IllegalStateException("User must be approved and in buyer mode to place bids");
        }
        
        if (auction.getSeller().getId().equals(bidder.getId())) {
            throw new IllegalStateException("Cannot bid on your own auction");
        }
        
        if (auction.getStatus() != Auction.AuctionStatus.LIVE) {
            throw new IllegalStateException("Can only bid on live auctions");
        }
        
        BigDecimal currentHighestBid = auction.getCurrentHighestBid();
        if (bidDTO.getBidAmount().compareTo(currentHighestBid) <= 0) {
            throw new IllegalArgumentException("Bid must be higher than current highest bid: " + currentHighestBid);
        }
        
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setBidAmount(bidDTO.getBidAmount());
        
        bid = bidRepository.save(bid);
        
        // Refresh auction to get updated bids list
        auction = auctionRepository.findById(auction.getId())
            .orElseThrow(() -> new IllegalArgumentException("Auction not found"));
        
        return bid;
    }
    
    public List<Bid> getBidsByAuction(Long auctionId) {
        return bidRepository.findByAuctionId(auctionId);
    }
    
    public List<Bid> getBidsByBidder(User bidder) {
        return bidRepository.findByBidderId(bidder.getId());
    }
    
    public BidResponseDTO convertToDTO(Bid bid) {
        return new BidResponseDTO(
            bid.getId(),
            bid.getAuction().getId(),
            bid.getBidder().getFullName(),
            bid.getBidAmount(),
            bid.getBidTime()
        );
    }
    
    public List<BidResponseDTO> getBidsByAuctionAsDTO(Long auctionId) {
        return getBidsByAuction(auctionId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}

