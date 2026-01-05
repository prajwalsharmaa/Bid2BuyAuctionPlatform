package com.bid2buy.dto;

import com.bid2buy.entity.Address;
import com.bid2buy.entity.User;

public class AuctionWinnerDTO {
    private User winner;
    private Address deliveryAddress;
    
    public AuctionWinnerDTO() {}
    
    public AuctionWinnerDTO(User winner, Address deliveryAddress) {
        this.winner = winner;
        this.deliveryAddress = deliveryAddress;
    }
    
    public User getWinner() {
        return winner;
    }
    
    public void setWinner(User winner) {
        this.winner = winner;
    }
    
    public Address getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}

