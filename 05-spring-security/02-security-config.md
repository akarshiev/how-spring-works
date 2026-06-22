# Security Config — SecurityFilterChain va HttpSecurity

`SecurityFilterChain` — Spring Security konfiguratsiyasining asosiy elementi. Qaysi URL'lar himoyalanganligi, qanday autentifikatsiya ishlatilishini bu yerda belgilaysiz.

## Asosiy konfiguratsiya

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // URL'larga ruxsat
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .anyRequest().authenticated()
            )
            // Login formasi (web ilovalar uchun)
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            )
            // Logout
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
```

## REST API uchun konfiguratsiya

JWT bilan stateless REST API:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())  // JWT bilan CSRF shart emas
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
        throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## URL'larga ruxsat qoidalari

```java
.authorizeHttpRequests(auth -> auth
    // Aniq URL
    .requestMatchers("/api/auth/login").permitAll()

    // Wildcard — /api/public/ va pastidagi barcha
    .requestMatchers("/api/public/**").permitAll()

    // HTTP metod + URL
    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")

    // Rol bo'yicha
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")

    // Authority bo'yicha
    .requestMatchers("/api/reports").hasAuthority("READ_REPORTS")

    // Faqat to'liq autentifikatsiya (Remember Me emas)
    .requestMatchers("/api/settings").fullyAuthenticated()

    // Qolgan hamma narsa — login kerak
    .anyRequest().authenticated()
)
```

Qoidalar yuqoridan pastga tekshiriladi — birinchi mos kelgani ishlaydi.

## PasswordEncoder

Parolni hech qachon ochiq saqlamang:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // Work factor default = 10
}

// Ishlatish
String encoded = passwordEncoder.encode("myPassword");
// → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

boolean matches = passwordEncoder.matches("myPassword", encoded);  // true
```

BCrypt har safar boshqacha hash hosil qiladi — xavfsiz. Brute force'ga chidamli.

## CORS — boshqa domenlardan so'rov

Frontend (localhost:3000) va backend (localhost:8080) alohida bo'lsa:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(
        "http://localhost:3000",  // Development
        "https://myapp.uz"        // Production
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Authorization"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

## CSRF — qachon o'chirish kerak?

CSRF (Cross-Site Request Forgery) himoyasi session asosida ishlaydi. Stateless JWT API'da CSRF kerak emas chunki:

- Cookie ishlatilmaydi — `Authorization` header ishlatiladi
- Soxta sayt `Authorization` headerini o'zgirtira olmaydi

```java
// REST API + JWT — CSRF o'chirish to'g'ri
.csrf(csrf -> csrf.disable())

// HTML form + Session — CSRF yoqilgan bo'lishi kerak
.csrf(Customizer.withDefaults())
```

## 401 va 403 xatolarini sozlash

```java
http
    .exceptionHandling(ex -> ex
        .authenticationEntryPoint((request, response, authException) -> {
            // 401 — login qilinmagan
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Login qilish talab etiladi\"}");
        })
        .accessDeniedHandler((request, response, accessDeniedException) -> {
            // 403 — ruxsat yo'q
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Ruxsat yo'q\"}");
        })
    )
```
