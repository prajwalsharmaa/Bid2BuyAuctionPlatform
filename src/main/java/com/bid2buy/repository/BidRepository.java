package com.bid2buy.repository;

import com.bid2buy.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByAuctionId(Long auctionId);
    List<Bid> findByBidderId(Long bidderId);
    
    @Query("SELECT b FROM Bid b WHERE b.auction.id = :auctionId AND b.bidder.id = :bidderId")
    List<Bid> findByAuctionIdAndBidderId(@Param("auctionId") Long auctionId, @Param("bidderId") Long bidderId);
}

