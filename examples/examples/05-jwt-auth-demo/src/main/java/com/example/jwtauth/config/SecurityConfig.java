package com.example.jwtauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Configuration = Springga "bu yerda sozlamalar bor" deb aytadi
// @EnableWebSecurity = Spring Security ni yoqish
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    // SecurityFilterChain = URL larga ruxsat berish qoidalari
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF (saytlararo soxta so'rov) -> REST API da kerak emas
            .csrf(csrf -> csrf.disable())

            // URL larga ruxsatlar
            .authorizeHttpRequests(auth -> auth
                // /api/auth/** -> hamma kirishi mumkin (login, register)
                .requestMatchers("/api/auth/**").permitAll()
                // /api/admin/** -> faqat ADMIN roli bilan
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Qolgan hamma URL -> login qilgan foydalanuvchi kerak
                .anyRequest().authenticated()
            )

            // Session = STATELESS (JWT ishlatsak, server sessiya saqlamaydi)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // JWT filterni UsernamePasswordAuthenticationFilter dan oldin ishga tushirish
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    // PasswordEncoder = parollarni shifrlash (BCrypt = kuchli shifrlash)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    // AuthenticationManager = login jarayonini boshqaradi
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
