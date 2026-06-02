package com.example.jwtauth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    // GET /api/me -> hozirgi foydalanuvchi malumotlari
    // Bu URL ga faqat login qilgan foydalanuvchi kira oladi
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        return ResponseEntity.ok(Map.of(
                "username", auth.getName(),
                "authorities", auth.getAuthorities().toString(),
                "authenticated", auth.isAuthenticated()
        ));
    }

    // GET /api/admin/dashboard -> faqat ADMIN lar uchun
    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> adminDashboard() {
        return ResponseEntity.ok(Map.of(
                "message", "Xush kelibsiz, Admin!",
                "data", "Bu yerda admin panel malumotlari"
        ));
    }
}
