package com.bid2buy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidResponseDTO {
    private Long id;
    private Long auctionId;
    private String bidderName;
    private BigDecimal bidAmount;
    private LocalDateTime bidTime;
    
    public BidResponseDTO() {}
    
    public BidResponseDTO(Long id, Long auctionId, String bidderName, BigDecimal bidAmount, LocalDateTime bidTime) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidderName = bidderName;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAuctionId() {
        return auctionId;
    }
    
    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }
    
    public String getBidderName() {
        return bidderName;
    }
    
    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }
    
    public BigDecimal getBidAmount() {
        return bidAmount;
    }
    
    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }
    
    public LocalDateTime getBidTime() {
        return bidTime;
    }
    
    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }
}

