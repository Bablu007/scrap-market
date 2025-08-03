package com.scrapmarket.scrap_market.common;

import com.scrapmarket.scrap_market.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class OTPServiceImpl {

    private final SecureRandom random = new SecureRandom();
    @Autowired
    private MailService mailUtil;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private  String otpKey = "otp:";
    private final Integer  TIME_OUT = 5;
   public String sendOtp(String email) {
        otpKey = otpKey + email;
       String requestCountKey = "otp_request_count:" + email;
       String blockKey = "otp_block:" + email;

       // 1. Check if the user is currently blocked
       if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
           throw new RuntimeException("You are temporarily blocked due to too many OTP requests. Try again after 1 hour.");
       }

       // 2. Check if an OTP already exists and is still valid
       if (redisTemplate.hasKey(otpKey)) {
           return "A valid OTP has already been sent to your email. Please check your inbox.";
       }

       // 3. Check how many times user has requested OTP today
       String requestCountStr = redisTemplate.opsForValue().get(requestCountKey);
       int requestCount = requestCountStr != null ? Integer.parseInt(requestCountStr) : 0;

       if (requestCount >= 10) {
           // Block user for 1 hour and stop further processing
           redisTemplate.opsForValue().set(blockKey, "BLOCKED", Duration.ofHours(1));
           throw new RuntimeException("You have exceeded the maximum OTP requests for today. Please try again after 1 hour.");
       }

       // 4. Generate OTP
       String otp = this.generateOtp();

       // 5. Send OTP email
       boolean isSent = mailUtil.sendOtpEmail(mailSender, email, otp);
       if (!isSent) {
           throw new RuntimeException("Failed to send OTP email. Please try again later.");
       }

       // 6. Store OTP with TTL (e.g., 5 minutes)
       this.storeOtp(redisTemplate, email, otp);

       // 7. Track request count with 24-hour expiration
       if (requestCount == 0) {
           redisTemplate.opsForValue().set(requestCountKey, "1", Duration.ofHours(24));
       } else {
           redisTemplate.opsForValue().increment(requestCountKey);
       }

       return "OTP sent successfully. Please check your email.";
   }

    public boolean validateOtp(String email, String inputOtp) {
         otpKey = otpKey+email.trim();
        String failureKey = "otp_failures:" + email;
        String blockKey = "otp_blocked:" + email;

        // Step 1: Check if user is blocked
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
            throw new RuntimeException("You are temporarily blocked due to multiple failed OTP attempts. Try again in 1 hour.");
        }
        // Step 2: Retrieve stored OTP
        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        if (storedOtp == null || !storedOtp.equals(inputOtp)) {
            // OTP invalid — increment failure count
            Long failures = redisTemplate.opsForValue().increment(failureKey);

            // Set 1-hour TTL if first failure
            if (failures == 1) {
                redisTemplate.expire(failureKey, Duration.ofHours(1));
            }
            // Block after 10 failures
            if (failures >= 10) {
                redisTemplate.opsForValue().set(blockKey, "BLOCKED", Duration.ofHours(1));
            }

            throw new RuntimeException("Invalid OTP. Please try again.");
        }
        redisTemplate.opsForValue().set("isValidOtp:" + email, "true", Duration.ofMinutes(TIME_OUT));
        redisTemplate.delete("OTP:" + email);
       // return true;
        // Step 3: OTP valid — cleanup failure counter
        redisTemplate.delete(failureKey);
        redisTemplate.delete(otpKey);
        return true;
    }

    //  Check if OTP already verified
    public boolean isOtpValidated(String email) {
        String verified = redisTemplate.opsForValue().get("isValidOtp:" + email);
        return verified != null && Boolean.parseBoolean(verified);
    }

    public void storeOtp(StringRedisTemplate redisTemplate, String email, String otp) {
        redisTemplate.opsForValue().set(otpKey + email, otp, TIME_OUT, TimeUnit.MINUTES);
    }

    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

}
