package com.scrapmarket.scrap_market.services.productserviceimpl;

import com.scrapmarket.scrap_market.dto.ProductDto;
import com.scrapmarket.scrap_market.entity.Product;
import com.scrapmarket.scrap_market.entity.User;
import com.scrapmarket.scrap_market.repository.ProductRepository;
import com.scrapmarket.scrap_market.repository.UserRepository;
import com.scrapmarket.scrap_market.services.productservice.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ProductDto createProduct(Product product) {
        // Optionally validate seller or other fields
       Long sellerId= product.getSeller().getId();
       User user= userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found with ID: " + sellerId));
        product.setSeller(user);
       Product savedProduct= productRepository.save(product);
        return  this.mapToProductDto(savedProduct);
    }

    @Override
    public ProductDto updateProduct(Long productId, Product updatedProduct) {
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setPrice(updatedProduct.getPrice());
        existing.setQuantity(updatedProduct.getQuantity());
        existing.setCategory(updatedProduct.getCategory());
        existing.setStatus(updatedProduct.getStatus());
       Product productedProduct=  productRepository.save(existing);
        return this.mapToProductDto(productedProduct);
    }

    @Override
    public void deleteProduct(Long productId) {
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(existing);
    }

    @Override
    public ProductDto getProductById(Long productId) {
        Product product= productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return this.mapToProductDto(product);
    }

    @Override
    public ProductDto getProductByName(String name) {
        Product product= productRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return this.mapToProductDto(product);

    }

    @Override
    public List<ProductDto> getAllProducts() {
        List<Product> productList = productRepository.findAll();
        return productList.stream()
                .map(this::mapToProductDto)
                .collect(Collectors.toList());
    }


    public ProductDto mapToProductDto(Product product) {
        if (product == null) {
            throw new RuntimeException("Failed to save product");
        }
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setQuantity(product.getQuantity());
        productDto.setCategory(product.getCategory());
        productDto.setStatus(product.getStatus());
        productDto.setCreatedAt(product.getCreatedAt().toString());
        return productDto;
    }
}
