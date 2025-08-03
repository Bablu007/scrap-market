package com.scrapmarket.scrap_market.repository;

import com.scrapmarket.scrap_market.entity.Address;
import com.scrapmarket.scrap_market.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
 Address findByUser(User user);
}
