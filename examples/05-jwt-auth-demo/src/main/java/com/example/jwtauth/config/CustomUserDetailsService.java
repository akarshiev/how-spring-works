package com.example.jwtauth.config;

import com.example.jwtauth.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// UserDetailsService = foydalanuvchini username boyicha database dan qidiradi
// Spring Security login qilganda shu serviceni ishlatadi
@Configuration
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Lambda funksiya: username berilsa, database dan UserDetails qaytaradi
        return username -> userRepository.findByUsername(username)
                // Agar topilmasa, UsernameNotFoundException tashlaydi
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User topilmadi: " + username
                ));
    }
}
