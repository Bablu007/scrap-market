package com.scrapmarket.scrap_market.controller;

import com.scrapmarket.scrap_market.dto.ProductDto;
import com.scrapmarket.scrap_market.entity.Product;
import com.scrapmarket.scrap_market.entity.ProductImage;
import com.scrapmarket.scrap_market.services.productservice.ProductService;
import com.scrapmarket.scrap_market.services.productserviceimpl.ProductImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductImageService imageService;
    private ProductService productService;

    public ProductController(ProductImageService imageService,ProductService productService) {
        this.imageService = imageService;
        this.productService = productService;
    }
    /**
     * Uploads an image for a product.
     *
     * @param file      the image file to upload
     * @param productId the ID of the product to associate with the image
     * @return a response entity containing the uploaded image details or an error message
     */
    @PostMapping("/upload/{productId}")
    public ResponseEntity<?> uploadImages(@RequestParam("file") List<MultipartFile> file, @PathVariable Long productId) {
        try {
             imageService.uploadImages(file, productId);
            return ResponseEntity.ok("uploaded");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Downloads an image by its ID.
     *
     * @param imageId the ID of the image to download
     * @return a response entity containing the image stream or an error message
     */
    @GetMapping("/download/{imageId}")
    public ResponseEntity<?> downloadImage(@PathVariable Long imageId) {
        try {
            InputStream image = imageService.downloadImage(imageId);
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Download failed: " + e.getMessage());
        }
    }

    /**
     * Deletes an image by its ID.
     *
     * @param imageId the ID of the image to delete
     * @return a response entity indicating success or failure
     */
    @DeleteMapping("/delete/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
            return ResponseEntity.ok("Image deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Delete failed: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER')")
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody Product product) {
        //product.getSeller()
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ProductDto> getProductByName(@PathVariable String name) {
        return ResponseEntity.ok(productService.getProductByName(name));
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

}
