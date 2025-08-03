package com.scrapmarket.scrap_market.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Random;

@Service
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;
    @Autowired
    public CaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public  String generateCaptcha(String userId) {
        String captchaRequestKey = "captcha:requests:" + userId;
        String blockKey = "captcha:block:" + userId;
        String answerKey = "captcha:answer:" + userId;
        // Block check
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
            throw new RuntimeException("You are blocked for 24 hours due to too many CAPTCHA requests.");
        }

        // Rate limit check
        String countStr = redisTemplate.opsForValue().get(captchaRequestKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;

        if (count >= 10) {
            redisTemplate.opsForValue().set(blockKey, "BLOCKED", Duration.ofHours(24));
            throw new RuntimeException("Max CAPTCHA requests exceeded. Try again in 24 hours.");
        }

        // Generate simple arithmetic
        int a = new Random().nextInt(9) + 1;
        int b = new Random().nextInt(9) + 1;
        String expression = a + " + " + b + " = ?";
        int answer = a + b;

        // Create image
        BufferedImage  image = new BufferedImage(120, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 120, 40);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(expression, 10, 25);
        g2d.dispose();

        // Convert image to Base64
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CAPTCHA image");
        }
        String base64Image = Base64.getEncoder().encodeToString(out.toByteArray());

        // Store the answer for validation later, with TTL of 5 minutes
        redisTemplate.opsForValue().set(answerKey, String.valueOf(answer), Duration.ofMinutes(5));

        // Increment request count
        if (count == 0) {
            redisTemplate.opsForValue().set(captchaRequestKey, "1", Duration.ofHours(24));
        } else {
            redisTemplate.opsForValue().increment(captchaRequestKey);
        }
        return base64Image;
    }

    public  boolean validateCaptcha(String userId, String userAnswer) {
        String answerKey = "captcha:answer:" + userId;
        String correctAnswer = redisTemplate.opsForValue().get(answerKey);
        if (correctAnswer == null) {
            throw new RuntimeException("Captcha expired or not generated.");
        }
        return correctAnswer.equals(userAnswer.trim());
    }


}
