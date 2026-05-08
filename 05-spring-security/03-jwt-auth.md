# JWT Authentication

## JWT nima?

JWT = JSON Web Token. Bu foydalanuvchi haqidagi malumotlarni saglaydigan maxsus token.

JWT ni "elektron pasport" deb tasavvur qiling. Pasportda sizning malumotlaringiz bor va uni soxtalashtirib bolmaydi.

## JWT qanday ishlaydi?

```
1. Foydalanuvchi login qiladi (username + password)
        |
2. Server tekshiradi: "Login va parol tog'rimi?"
        |
3. Server JWT yaratadi va foydalanuvchiga beradi
        |
4. Foydalanuvchi har bir so'rovda JWT ni yuboradi
   (Header: "Authorization: Bearer <token>")
        |
5. Server JWT ni tekshiradi: "Bu token haqiqiymi?"
        |
6. Token haqiqiy bolsa -> so'rovni bajaradi
```

## JWT tuzilishi

JWT 3 qismdan iborat:

```
HEADER.PAYLOAD.SIGNATURE

eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGkiLCJyb2xlIjoiQURNSU4ifQ.dGhpcyBpc...
```

### 1. Header - Token haqida malumot

```json
{
    "alg": "HS256",        // Shifrlash algoritmi
    "typ": "JWT"           // Token turi
}
```

### 2. Payload - Foydalanuvchi malumotlari

```json
{
    "sub": "ali",          // Foydalanuvchi nomi
    "role": "ADMIN",       // Roli
    "iat": 1712345678,     // Yaratilgan vaqt
    "exp": 1712432078      // Muddati tugash vaqti
}
```

### 3. Signature - Imzo (soxtalashtirishdan himoya)

Signature = Header + Payload + SecretKey yordamida yaratiladi.

Agar kimdir Payload ni ozgartirsa -> Signature notogri boladi -> Server tokenni qabul qilmaydi.

## JWT implementatsiyasi

### 1-qadam: Dependency qoshish

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

### 2-qadam: JWT Service

```java
@Component
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long expirationMs;
    
    // Token yaratish
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .subject(userDetails.getUsername())
            .claim("role", userDetails.getAuthorities())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey())
            .compact();
    }
    
    // Tokendan username olish
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }
    
    // Token haqiqiymi?
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    
    private Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
```

### 3-qadam: JWT Filter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### 4-qadam: SecurityConfig da filterni qoshish

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 5-qadam: Auth Controller

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. Autentifikatsiya
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        // 2. JWT yaratish
        String token = jwtService.generateToken((UserDetails) auth.getPrincipal());
        
        // 3. Token ni qaytarish
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
```

## Xulosa

- JWT = foydalanuvchi malumotlari shifrlangan token
- Server sessiya saqlamaydi (stateless)
- Foydalanuvchi har so'rovda JWT yuboradi
- JWT 3 qism: header + payload + signature
- Signature token soxtalashtirishdan himoya qiladi
