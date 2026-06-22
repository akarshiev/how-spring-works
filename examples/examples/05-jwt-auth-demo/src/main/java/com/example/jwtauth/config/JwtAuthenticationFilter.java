package com.example.jwtauth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// @Component = Spring bu filterni avtomatik topadi
// OncePerRequestFilter = har bir HTTP so'rovda 1 marta ishlaydi
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. "Authorization" header dan tokenni olish
        // Header: "Authorization: Bearer <token>"
        String authHeader = request.getHeader("Authorization");

        // Agar header bolmasa yoki "Bearer " bilan boshlamasa -> keyingi filterga ot
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " dan keyingi qism = token
        String token = authHeader.substring(7);

        // 2. Tokendan username ni olish
        String username = jwtService.extractUsername(token);

        // 3. Agar username bor va foydalanuvchi hali autentifikatsiya qilinmagan bolsa
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Username boyicha foydalanuvchini database dan olish
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 4. Token haqiqiyligini tekshirish
            if (jwtService.isTokenValid(token, userDetails)) {

                // Authentication obektini yaratish
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,  // credentials (parol) - token da kerak emas
                                userDetails.getAuthorities()
                        );

                // Request malumotlarini qoshish (IP, session, ...)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Foydalanuvchini "login qilgan" deb belgilash
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 5. Keyingi filterga otish
        filterChain.doFilter(request, response);
    }
}
