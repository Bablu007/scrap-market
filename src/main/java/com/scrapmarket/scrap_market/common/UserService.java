package com.scrapmarket.scrap_market.common;

import com.scrapmarket.scrap_market.dto.AddressDTO;
import com.scrapmarket.scrap_market.dto.TokenResponse;
import com.scrapmarket.scrap_market.entity.User;

public interface UserService {
    String registerUser(User user);

    TokenResponse loginUser(String email, String password);

    TokenResponse generateRefreshToken(String refreshToken);

    String getUserDetails(String token);

    void deleteUser(String email);

     void saveCurrentAddress(String email,AddressDTO dto);

     AddressDTO getCurrentAddress(String email);

     void deleteCurrentAddress(String email);

}
