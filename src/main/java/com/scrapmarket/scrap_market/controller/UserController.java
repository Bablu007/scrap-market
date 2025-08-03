package com.scrapmarket.scrap_market.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/details")
    @PreAuthorize("hasRole('USER')")
    public String getUser() {
        return "user";
    }
}
