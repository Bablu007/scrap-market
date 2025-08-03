package com.scrapmarket.scrap_market.services.productservice;

import com.scrapmarket.scrap_market.dto.ProductDto;
import com.scrapmarket.scrap_market.entity.Product;

import java.util.List;

public interface ProductService {
    ProductDto createProduct(Product product);
    ProductDto updateProduct(Long productId, Product updatedProduct);
    void deleteProduct(Long productId);
    ProductDto getProductById(Long productId);
    ProductDto getProductByName(String name);
    List<ProductDto> getAllProducts();
}

