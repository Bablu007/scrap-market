package com.scrapmarket.scrap_market.common;

import com.scrapmarket.scrap_market.entity.ProductImage;
import com.scrapmarket.scrap_market.repository.ProductImageRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import io.minio.RemoveObjectArgs;
import java.net.URL;
@Service
public class MinioFileUploadService {
    private final ProductImageRepository imageRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public MinioFileUploadService(MinioClient minioClient,ProductImageRepository imageRepository) {
        this.minioClient = minioClient;
        this.imageRepository = imageRepository;
    }

    public String uploadFile(MultipartFile file,String objectName) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        return "File uploaded: " + objectName;
    }

    public InputStream downloadFile(String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    public void deleteImage(Long imageId) throws Exception {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // Extract object name from URL (e.g., "product-images/xyz.png")
        String objectUrl = image.getImageUrl();
        URL url = new URL(objectUrl);
        String objectName = url.getPath().substring(1); // skip leading '/'
        // Remove from MinIO
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        imageRepository.delete(image);
    }
}
