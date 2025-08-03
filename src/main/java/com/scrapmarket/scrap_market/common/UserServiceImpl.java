package com.scrapmarket.scrap_market.common;

import com.scrapmarket.scrap_market.dto.AddressDTO;
import com.scrapmarket.scrap_market.dto.TokenResponse;
import com.scrapmarket.scrap_market.entity.Address;
import com.scrapmarket.scrap_market.entity.User;
import com.scrapmarket.scrap_market.enums.Role;
import com.scrapmarket.scrap_market.exception.UserAlreadyExistsException;
import com.scrapmarket.scrap_market.exception.UsernameNotFoundException;
import com.scrapmarket.scrap_market.repository.AddressRepository;
import com.scrapmarket.scrap_market.repository.UserRepository;
import com.scrapmarket.scrap_market.security.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    @Autowired
    MailService otpServiceUtil;
    @Autowired
    OTPServiceImpl otpServiceImpl;
    @Autowired
    ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
   // private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
   // private final StringRedisTemplate redisTemplate;
    private JavaMailSender mailSender;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, JavaMailSender javaMailSender, AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
       // this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.mailSender = javaMailSender;
    }

    @Override
    public String registerUser(User user) {
        EnumSet<Role> allowedRoles = EnumSet.of(Role.BUYER, Role.SELLER, Role.ADMIN);
        if (allowedRoles.contains(user.getRole())) {
            user.setRole(user.getRole());
        } else {
            user.setRole(String.valueOf(Role.BUYER)); // default fallback
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        if (!otpServiceImpl.isOtpValidated(user.getEmail())) {
            return "OTP not verified. Please verify the OTP to complete registration.";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully.";
    }

    @Override
    public TokenResponse loginUser(String email, String password) {
        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);
        return new TokenResponse(accessToken, refreshToken);
    }

    @Override
    public TokenResponse generateRefreshToken(String refreshToken) {
        if (jwtUtil.validateToken(refreshToken)) {
            String email = jwtUtil.extractUsername(refreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(email);
            return new TokenResponse(newAccessToken, refreshToken);
        } else {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    @Override
    public String getUserDetails(String token) {
        // Logic to fetch user details using token
        return "User details fetched successfully";
    }

    @Override
    public void deleteUser(String email) {
        // Logic to delete a user by email
    }

    @Override
    public void saveCurrentAddress(String email, AddressDTO dto) {
        this.addOrUpdateCurrentAddress(email, dto);;
    }

    @Override
    public AddressDTO getCurrentAddress(String email) {
        User user=userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
       Address address= addressRepository.findByUser(user);
      return  modelMapper.map(address,AddressDTO.class);
    }

    @Override
    public void deleteCurrentAddress(String email) {
        // Logic to delete the current address of a user by email
    }


    public void addOrUpdateCurrentAddress(String email, AddressDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        Address existingAddress = addressRepository.findByUser(user);
        if (existingAddress != null) {
            // Update existing address
            existingAddress.setStreet(dto.getStreet());
            existingAddress.setCity(dto.getCity());
            existingAddress.setState(dto.getState());
            existingAddress.setCountry(dto.getCountry());
            existingAddress.setPostalCode(dto.getPostalCode());
            addressRepository.save(existingAddress);
        } else {
            // Create new address
            Address newAddress = new Address();
            newAddress.setStreet(dto.getStreet());
            newAddress.setCity(dto.getCity());
            newAddress.setState(dto.getState());
            newAddress.setCountry(dto.getCountry());
            newAddress.setPostalCode(dto.getPostalCode());
            //newAddress.isPrimary(AddressType.CURRENT);
            newAddress.setUser(user);
            addressRepository.save(newAddress);
        }
    }


}

