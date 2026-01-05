package com.bid2buy.repository;

import com.bid2buy.entity.Address;
import com.bid2buy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByIdAndUserId(Long id, Long userId);
    Optional<Address> findByUserIdAndIsDefaultShippingTrue(Long userId);
    Optional<Address> findByUserIdAndIsDefaultBillingTrue(Long userId);
}

