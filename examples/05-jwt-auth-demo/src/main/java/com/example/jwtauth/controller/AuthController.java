package com.example.jwtauth.controller;

import com.example.jwtauth.config.JwtService;
import com.example.jwtauth.model.User;
import com.example.jwtauth.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// @RestController = REST API
// @RequestMapping("/api/auth") = hamma URL "/api/auth" bilan boshlanadi
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Constructor Injection -> Spring 3 ta bogliqlikni avtomatik beradi
    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // POST /api/auth/register -> yangi foydalanuvchi yaratish
    // Body: {"username":"ali","email":"ali@example.com","password":"123"}
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Email yoki username allaqachon mavjudligini tekshirish
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bu username allaqachon mavjud"));
        }
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bu email allaqachon mavjud"));
        }

        // Yangi foydalanuvchi yaratish
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        // Parolni BCrypt bilan shifrlash (ochiq matn sifatida saqlanmaydi!)
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(User.Role.USER);  // Default rol = USER

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Foydalanuvchi muvaffaqiyatli yaratildi"));
    }

    // POST /api/auth/login -> login qilish
    // Body: {"username":"ali","password":"123"}
    // Javob: {"token": "...JWT token...", "username": "ali", "role": "USER"}
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Foydalanuvchini database dan topish
        User user = userRepository.findByUsername(request.username())
                .orElse(null);

        // Agar foydalanuvchi topilmasa -> 401 (Unauthorized)
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Username yoki parol notogri"));
        }

        // Parolni tekshirish (yozilgan parol -> shifrlangan parol bilan solishtirish)
        // passwordEncoder.matches() = yozilgan parolni shifrlab, database dagi bilan solishtiradi
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Username yoki parol notogri"));
        }

        // Agar hammasi togri bolsa -> JWT token yaratish
        String token = jwtService.generateToken(user);

        // JWT token va foydalanuvchi malumotlarini qaytarish
        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole().name()
        ));
    }

    // ============ DTO lar ============

    // record = Java 16+ da qisqa DTO yaratish usuli
    // avtomatik ravishda: constructor, getter, toString, equals, hashCode yaratadi
    public record RegisterRequest(String username, String email, String password) {}
    public record LoginRequest(String username, String password) {}
}
