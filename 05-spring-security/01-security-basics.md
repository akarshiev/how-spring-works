# Security Basics — Authentication va Authorization

Spring Security ilovangizni himoya qiluvchi framework. Har bir so'rovni tekshiradi: "Bu kishi kim? Nima qila oladi?"

## Ikki asosiy tushuncha

### Authentication — Kim bu?

Foydalanuvchi identifikatsiyasi. "Siz kimligingizni isboting."

```
Foydalanuvchi: "Ali man, parolim 12345"
Server:        "Tekshirdim — to'g'ri. Siz Alisiz. Kiruvingiz mumkin."
```

Autentifikatsiya usullari: username/password, JWT token, OAuth2, API key, biometrik.

### Authorization — Nima qila olasiz?

Autentifikatsiyadan keyin — nima qilish ruxsati borligini tekshirish.

```
Ali: "Admin panelini ochmoqchiman"
Server: "Siz autentifikatsiya bo'lgansiz, lekin admin emassiz. Ruxsat yo'q."
```

Autentifikatsiya → "Siz Alisiz" → true/false  
Authorization → "Siz admin sahifasini ko'ra olasizmi?" → true/false

## Spring Security Filter Chain

Spring Security filterlar zanjiri orqali ishlaydi. Har bir HTTP so'rov bir nechta filterdan o'tadi:

```
HTTP So'rov
     |
     v
[SecurityContextPersistenceFilter]  ← mavjud autentifikatsiyani tiklash
     |
     v
[UsernamePasswordAuthenticationFilter]  ← login formani tekshirish
     |
     v
[BearerTokenAuthenticationFilter]  ← JWT tokenni tekshirish
     |
     v
[ExceptionTranslationFilter]  ← 401/403 xatolarni tarjima qilish
     |
     v
[AuthorizationFilter]  ← ruxsatni tekshirish
     |
     v
Controller  ← faqat barcha filterdan o'tsa
```

## SecurityContext — kim kirganini esda saqlash

Autentifikatsiyadan o'tgan foydalanuvchi ma'lumotlari `SecurityContext`'da saqlanadi (thread-local):

```java
// Hozirgi foydalanuvchini olish
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();                          // "ali"
Collection<?> authorities = auth.getAuthorities();        // [ROLE_USER]
boolean isAuthenticated = auth.isAuthenticated();         // true

// Controller'da qulay usul
@GetMapping("/me")
public UserResponse currentUser(@AuthenticationPrincipal UserDetails userDetails) {
    return userService.findByUsername(userDetails.getUsername());
}
```

## Rollar va Huquqlar (Roles vs Authorities)

Spring Security'da ikkita tushuncha bor:

**Role** — `ROLE_` prefiksi bilan: `ROLE_USER`, `ROLE_ADMIN`. `hasRole("ADMIN")` → `ROLE_ADMIN` ni tekshiradi.

**Authority** — ixtiyoriy string: `"READ_USERS"`, `"WRITE_PRODUCTS"`. `hasAuthority("READ_USERS")` ni tekshiradi.

```java
// URL'ga ruxsat
.requestMatchers("/admin/**").hasRole("ADMIN")        // ROLE_ADMIN kerak
.requestMatchers("/api/users").hasAuthority("READ_USERS")  // Aniq authority

// Metod darajasida
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@PreAuthorize("hasAuthority('WRITE')")
```

## Oddiy konfiguratsiya

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()     // Login/register — hamma
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()                    // Qolgan — login kerak
            )
            .csrf(csrf -> csrf.disable())  // REST API'da odatda o'chiriladi
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // JWT uchun
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Parolni shifrlash
    }
}
```

Keyingi bo'limlarda `SecurityFilterChain`, JWT va `UserDetailsService` batafsil ko'riladi.
