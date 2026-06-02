package com.example.jwtauth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// @Service = JWT token yaratish va tekshirish xizmati
@Service
public class JwtService {

    @Value("${jwt.secret}")       // application.properties dan secret key ni olish
    private String secretKey;

    @Value("${jwt.expiration}")   // application.properties dan muddatni olish
    private long expirationMs;

    // ============ TOKEN YARATISH ============

    // UserDetails dan foydalanib JWT token yaratish
    public String generateToken(UserDetails userDetails) {
        // extraClaims = token ichiga qoshimcha malumotlar (masalan rol)
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", userDetails.getAuthorities());

        return Jwts.builder()
                .claims(extraClaims)                          // Qoshimcha malumotlar
                .subject(userDetails.getUsername())           // Token egasi (username)
                .issuedAt(new Date())                         // Yaratilgan vaqt
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // Muddati
                .signWith(getSigningKey())                    // Imzolash (secret key bilan)
                .compact();                                   // String ga aylantirish
    }

    // ============ TOKEN DAN MALUMOT OLISH ============

    // Tokendan username ni ajratib olish
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Token muddati tugaganmi?
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // Token haqiqiymi va username tog'rimi?
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ============ ICHKI METODLAR ============

    // JWT ni ochish va ichidagi malumotlarni olish
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())    // Secret key bilan tekshirish
                .build()
                .parseSignedClaims(token)       // Tokenni ochish
                .getPayload();                  // Ichidagi malumotlarni olish
    }

    // SecretKey ni yaratish (Base64 dan HS256 kalitga)
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
