package com.bid2buy.repository;

import com.bid2buy.entity.Auction;
import com.bid2buy.entity.Auction.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByStatus(AuctionStatus status);
    List<Auction> findByCategoryId(Long categoryId);
    List<Auction> findBySellerId(Long sellerId);
    
    @Query("SELECT a FROM Auction a WHERE LOWER(a.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) AND a.status != 'ENDED'")
    List<Auction> searchByProductName(@Param("keyword") String keyword);
    
    @Query("SELECT a FROM Auction a WHERE a.category.id = :categoryId AND LOWER(a.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) AND a.status != 'ENDED'")
    List<Auction> searchByCategoryAndProductName(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);
    
    @Query("SELECT a FROM Auction a WHERE a.category.id = :categoryId AND a.status != 'ENDED'")
    List<Auction> findByCategoryIdExcludingEnded(@Param("categoryId") Long categoryId);
}

