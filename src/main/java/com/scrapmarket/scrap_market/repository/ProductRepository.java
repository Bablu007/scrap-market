package com.scrapmarket.scrap_market.repository;

import com.scrapmarket.scrap_market.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Additional query methods can be defined here if needed
    Optional<Product> findByName(String name);
}
