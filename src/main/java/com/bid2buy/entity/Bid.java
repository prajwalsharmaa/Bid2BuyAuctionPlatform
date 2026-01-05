package com.bid2buy.entity;

import jakarta.persistence.*;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class Bid {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    @NotNull(message = "Auction is required")
    private Auction auction;
    
    @ManyToOne
    @JoinColumn(name = "bidder_id", nullable = false)
    @NotNull(message = "Bidder is required")
    private User bidder;
    
    @Column(name = "bid_amount", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    private BigDecimal bidAmount;
    
    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime = LocalDateTime.now();
    
    // Constructors
    public Bid() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Auction getAuction() {
        return auction;
    }
    
    public void setAuction(Auction auction) {
        this.auction = auction;
    }
    
    public User getBidder() {
        return bidder;
    }
    
    public void setBidder(User bidder) {
        this.bidder = bidder;
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

