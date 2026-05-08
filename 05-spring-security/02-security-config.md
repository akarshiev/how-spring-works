# Security Configuration - SecurityFilterChain va HttpSecurity

## SecurityFilterChain nima?

SecurityFilterChain = Spring Security ni sozlash uchun asosiy klass.

Bu klass yordamida siz:

- Qaysi URL larga ruxsat borligini
- Qanday login turi ishlatilishini
- CSRF yoqilganmi yoki yoqmi
- Va boshqa sozlamalarni belgilaysiz

## Asosiy konfiguratsiya

```java
@Configuration
@EnableWebSecurity  // Spring Security ni yoqish
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/login", "/register").permitAll() // hamma kirishi mumkin
                .requestMatchers("/admin/**").hasRole("ADMIN")                    // faqat admin
                .requestMatchers("/user/**").hasRole("USER")                      // faqat user
                .anyRequest().authenticated()                                     // qolgan hamma narsa login talab
            )
            .formLogin(form -> form
                .loginPage("/login")          // login sahifasi
                .defaultSuccessUrl("/home")   // muvaffaqiyatli login dan keyin
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login")   // logout dan keyin
                .permitAll()
            );
        
        return http.build();
    }
}
```

## Hamma URL larni himoya qilish

| Sozlamalar | Nima qiladi? |
|-----------|-------------|
| `.permitAll()` | Hech qanday tekshirish yo'q |
| `.authenticated()` | Faqat login qilganlar uchun |
| `.hasRole("ADMIN")` | Faqat ADMIN roli bilan |
| `.hasAnyRole("USER", "ADMIN")` | Bir nechta roldan biri |
| `.hasAuthority("WRITE")` | Muayyan authority bilan |
| `.denyAll()` | Hech kimga ruxsat yo'q |

## PasswordEncoder - Parollarni shifrlash

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // BCrypt = kuchli shifrlash
}
```

Ishlatish:

```java
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    
    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    public User register(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));  // Parol shifrlanadi
        return userRepository.save(user);
    }
}
```

## CSRF - Saytlararo soxta so'rov

CSRF = Cross-Site Request Forgery. Bu boshqa saytdan sizning saytingizga yuborilgan soxta so'rovlardan himoya.

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf
            .disable()  // REST API larda ko'pincha off (JWT token ishlatilsa)
        )
        // ... qolgan sozlamalar
}
```

**API larda** -> CSRF odatda OFF (JWT/Token ishlatiladi)
**HTML formalarda** -> CSRF ON bolishi kerak

## CORS - Boshqa domenlardan so'rov

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors
            .configurationSource(corsConfigurationSource())
        )
        // ...
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000")); // Frontend domeni
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(List.of("*"));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

## To'liq konfiguratsiya misoli

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT uchun
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
```

## Xulosa

- SecurityFilterChain -> Spring Security ni sozlash uchun markaziy klass
- authorizeHttpRequests -> URL larga ruxsat
- PasswordEncoder -> parollarni shifrlash
- CSRF -> REST API larda disable
- CORS -> frontend domeniga ruxsat
