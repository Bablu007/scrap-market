package com.scrapmarket.scrap_market.repository;

import com.scrapmarket.scrap_market.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    // Additional query methods can be defined here if needed

}
