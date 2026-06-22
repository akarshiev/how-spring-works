# JWT Authentication

JWT (JSON Web Token) — stateless autentifikatsiya uchun eng keng tarqalgan yondashuv. Server sessiya saqlamaydi — token o'zi hamma ma'lumotni o'z ichida saqlaydi.

## JWT qanday ishlaydi?

```
1. Client login qiladi → username + password yuboradi
              |
2. Server tekshiradi → To'g'ri bo'lsa JWT yaratadi va qaytaradi
              |
3. Client JWT'ni saqlaydi (localStorage, memory)
              |
4. Har bir so'rovda Header ga qo'shadi:
   Authorization: Bearer eyJhbGci...
              |
5. Server JWT'ni tekshiradi → token haqiqiy va muddati o'tmagan bo'lsa
              |
6. So'rov bajariladi
```

## JWT tuzilishi

JWT nuqta bilan ajratilgan uchta qismdan iborat:

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGkiLCJleHAiOjE3MTIzNDU2Nzh9.ABC123
       ↑                         ↑                              ↑
   HEADER                    PAYLOAD                       SIGNATURE
```

**Header** — algoritm va token turi:
```json
{"alg": "HS256", "typ": "JWT"}
```

**Payload** — ma'lumotlar (claims):
```json
{
  "sub": "ali",              // Subject — kim uchun
  "role": "USER",            // Custom claim
  "iat": 1712345678,         // Issued at
  "exp": 1712432078          // Expiration
}
```

**Signature** — `HMAC(header + "." + payload, secretKey)`. Bu qism token soxtalashtirilmaganini kafolatlaydi.

## Implementatsiya

### 1. Dependency

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

### 2. JWT Service

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}")  // Default: 24 soat
    private long expirationMs;

    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(userDetails.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
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
}
```

### 3. JWT Filter

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

        // Token yo'q yoki noto'g'ri format — davom et
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);

            if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException e) {
            // Token noto'g'ri — davom et (keyingi filter 401 qaytaradi)
            log.warn("Invalid JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
```

### 4. Auth Controller

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Spring Security autentifikatsiya qiladi
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        // Xato bo'lsa — BadCredentialsException tashlanadi

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(userService.register(request));
    }
}
```

### 5. application.properties

```properties
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration-ms=86400000
```

Secret — kamida 256-bit (32 byte) bo'lishi kerak. `openssl rand -base64 32` bilan generatsiya qiling.

## Refresh Token

Access token (qisqa muddat: 15 daqiqa–1 soat) va refresh token (uzun muddat: 7–30 kun):

```java
@PostMapping("/refresh")
public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
    String username = jwtService.extractUsername(request.getRefreshToken());
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    if (jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
        String newToken = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(newToken));
    }

    throw new InvalidTokenException("Refresh token noto'g'ri yoki muddati o'tgan");
}
```
