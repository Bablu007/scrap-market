package com.scrapmarket.scrap_market.controller;

import com.scrapmarket.scrap_market.services.productserviceimpl.ProductImageService;
import com.scrapmarket.scrap_market.dto.*;
import com.scrapmarket.scrap_market.entity.ProductImage;
import com.scrapmarket.scrap_market.entity.User;
import com.scrapmarket.scrap_market.common.OTPServiceImpl;
import com.scrapmarket.scrap_market.common.UserServiceImpl;
import com.scrapmarket.scrap_market.common.CaptchaService;
import com.scrapmarket.scrap_market.util.RsaUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);
    private final UserServiceImpl userServiceImpl;
    private final AuthenticationManager authenticationManager;
    private final OTPServiceImpl otpServiceImpl;
    private final CaptchaService captchaUtil;
    @Autowired
    public AuthController(UserServiceImpl userServiceImpl, AuthenticationManager authenticationManager, OTPServiceImpl otpServiceImpl, CaptchaService captchaUtil) {
        this.userServiceImpl = userServiceImpl;
        this.authenticationManager = authenticationManager;
        this.otpServiceImpl = otpServiceImpl;
        this.captchaUtil = captchaUtil;
    }
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
       String response=  userServiceImpl.registerUser(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Validated @RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse;
        try {
            String decryptedPassword = RsaUtil.decrypt(loginRequest.getPassword(), RsaUtil.getPrivateKey());
            String decryptedEmail = RsaUtil.decrypt(loginRequest.getEmail(), RsaUtil.getPrivateKey());
            logger.info("Decrypted Email: {}", decryptedEmail);
            logger.info("Decrypted Password: {}", decryptedPassword);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            decryptedEmail,
                            decryptedPassword)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            tokenResponse = userServiceImpl.loginUser(decryptedEmail, decryptedPassword);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Decryption failed: " + e.getMessage());
        }
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestParam String refreshToken) {
        return ResponseEntity.ok(userServiceImpl.generateRefreshToken(refreshToken));
    }

    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() throws Exception {
        return ResponseEntity.ok(RsaUtil.getEncodedPublicKey());
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<String> validateOtp(@RequestBody OtpRequest otpRequest) {
        boolean isValid = otpServiceImpl.validateOtp(otpRequest.getEmail(), otpRequest.getOtp());
        if (isValid) {
            return ResponseEntity.ok("OTP is valid");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        return ResponseEntity.ok(otpServiceImpl.sendOtp(email));
    }

    @PostMapping("/generate-captcha")
    public ResponseEntity<String> generateCaptcha(@RequestParam String userId) {
        String captchaValue = captchaUtil.generateCaptcha(userId);
        return ResponseEntity.ok(captchaValue);
    }

    @PostMapping("/validate-captcha")
    public ResponseEntity<String> validateCaptcha(@RequestBody ValidateCaptchaRequest captchaRequest) {
        boolean isValid = captchaUtil.validateCaptcha(captchaRequest.getUserId(),captchaRequest.getAnswer());
        if (isValid) {
            return ResponseEntity.ok("Captcha is valid");
        } else {
            return ResponseEntity.badRequest().body("Invalid captcha");
        }
    }



    @PostMapping("/save-address")
    public ResponseEntity<?> saveCurrentAddress(@RequestParam String email, @RequestBody AddressDTO dto) {
        userServiceImpl.addOrUpdateCurrentAddress(email, dto);
        return ResponseEntity.ok("Address saved successfully: ");
    }

    @GetMapping("/get-address")
    public ResponseEntity<?> getCurrentAddress(@RequestParam String email) {
        return ResponseEntity.ok(userServiceImpl.getCurrentAddress(email));
    }



}

