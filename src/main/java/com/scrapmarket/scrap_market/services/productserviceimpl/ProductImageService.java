package com.scrapmarket.scrap_market.services.productserviceimpl;

import com.scrapmarket.scrap_market.common.MinioFileUploadService;
import com.scrapmarket.scrap_market.entity.Product;
import com.scrapmarket.scrap_market.entity.ProductImage;
import com.scrapmarket.scrap_market.repository.ProductImageRepository;
import com.scrapmarket.scrap_market.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductImageService {
    private final MinioFileUploadService minioClient;
    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;


    public ProductImageService(ProductImageRepository imageRepository,
                               ProductRepository productRepository,MinioFileUploadService minioClient) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
        this.minioClient = minioClient;
    }

    public List<ProductImage> uploadImages(List<MultipartFile> files, Long productId) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductImage> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            String objectName = "product-images/" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
            String fileUrl = minioClient.uploadFile(file, objectName);

            ProductImage image = new ProductImage();
            image.setImageUrl(fileUrl);
            image.setProduct(product);

            savedImages.add(image);
        }

        return imageRepository.saveAll(savedImages);
    }


    public InputStream downloadImage(Long imageId) throws Exception {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
        String objectName = image.getImageUrl();
        return minioClient.downloadFile(objectName);
    }

    public void deleteImage(Long imageId) throws Exception {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
        minioClient.deleteImage(imageId);
        imageRepository.delete(image);
    }
}
