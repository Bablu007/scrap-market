package com.scrapmarket.scrap_market.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
public class AdminController {
    @GetMapping("/details")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminUser() {
        return "Admin";
    }
}
