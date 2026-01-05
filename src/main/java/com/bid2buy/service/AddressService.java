package com.bid2buy.service;

import com.bid2buy.dto.AddressDTO;
import com.bid2buy.entity.Address;
import com.bid2buy.entity.User;
import com.bid2buy.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressService {
    
    @Autowired
    private AddressRepository addressRepository;
    
    public List<Address> getAddressesByUser(User user) {
        return addressRepository.findByUserId(user.getId());
    }
    
    public Address getAddressById(Long id, User user) {
        return addressRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Address not found"));
    }
    
    public Address createAddress(User user, AddressDTO addressDTO) {
        Address address = new Address();
        address.setUser(user);
        address.setFullName(addressDTO.getFullName());
        address.setAddressLine(addressDTO.getAddressLine());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());
        address.setPhoneNumber(addressDTO.getPhoneNumber());
        address.setAddressType(addressDTO.getAddressType());
        
        // If this is set as default shipping, unset others
        if (addressDTO.getIsDefaultShipping() != null && addressDTO.getIsDefaultShipping()) {
            unsetDefaultShipping(user);
            address.setIsDefaultShipping(true);
        } else {
            address.setIsDefaultShipping(false);
        }
        
        // If this is set as default billing, unset others
        if (addressDTO.getIsDefaultBilling() != null && addressDTO.getIsDefaultBilling()) {
            unsetDefaultBilling(user);
            address.setIsDefaultBilling(true);
        } else {
            address.setIsDefaultBilling(false);
        }
        
        return addressRepository.save(address);
    }
    
    public Address updateAddress(Long id, User user, AddressDTO addressDTO) {
        Address address = getAddressById(id, user);
        
        address.setFullName(addressDTO.getFullName());
        address.setAddressLine(addressDTO.getAddressLine());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());
        address.setPhoneNumber(addressDTO.getPhoneNumber());
        address.setAddressType(addressDTO.getAddressType());
        
        // Handle default shipping
        if (addressDTO.getIsDefaultShipping() != null && addressDTO.getIsDefaultShipping()) {
            if (!address.getIsDefaultShipping()) {
                unsetDefaultShipping(user);
                address.setIsDefaultShipping(true);
            }
        } else {
            address.setIsDefaultShipping(false);
        }
        
        // Handle default billing
        if (addressDTO.getIsDefaultBilling() != null && addressDTO.getIsDefaultBilling()) {
            if (!address.getIsDefaultBilling()) {
                unsetDefaultBilling(user);
                address.setIsDefaultBilling(true);
            }
        } else {
            address.setIsDefaultBilling(false);
        }
        
        return addressRepository.save(address);
    }
    
    public void deleteAddress(Long id, User user) {
        Address address = getAddressById(id, user);
        addressRepository.delete(address);
    }
    
    public void setDefaultShipping(Long id, User user) {
        unsetDefaultShipping(user);
        Address address = getAddressById(id, user);
        address.setIsDefaultShipping(true);
        addressRepository.save(address);
    }
    
    public void setDefaultBilling(Long id, User user) {
        unsetDefaultBilling(user);
        Address address = getAddressById(id, user);
        address.setIsDefaultBilling(true);
        addressRepository.save(address);
    }
    
    private void unsetDefaultShipping(User user) {
        addressRepository.findByUserIdAndIsDefaultShippingTrue(user.getId())
            .ifPresent(address -> {
                address.setIsDefaultShipping(false);
                addressRepository.save(address);
            });
    }
    
    private void unsetDefaultBilling(User user) {
        addressRepository.findByUserIdAndIsDefaultBillingTrue(user.getId())
            .ifPresent(address -> {
                address.setIsDefaultBilling(false);
                addressRepository.save(address);
            });
    }
}

