# UserDetails va UserDetailsService

Spring Security o'z foydalanuvchi modeliga ega — `UserDetails`. Sizning `User` Entity'ngiz Spring Security bilan ishlashi uchun bu interfeysi implement qilishi yoki adapter yaratish kerak.

## UserDetails interfeysi

```java
public interface UserDetails extends Serializable {
    String getUsername();
    String getPassword();
    Collection<? extends GrantedAuthority> getAuthorities();  // Rollar/huquqlar
    boolean isAccountNonExpired();     // Hisob muddati o'tmaganmi?
    boolean isAccountNonLocked();      // Hisob bloklangan emas?
    boolean isCredentialsNonExpired(); // Parol muddati o'tmaganmi?
    boolean isEnabled();               // Hisob faolmi?
}
```

## UserDetailsService interfeysi

```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
```

Spring Security autentifikatsiya vaqtida `loadUserByUsername()` chaqiradi.

## 1-yondashuv: Entity UserDetails'ni implement qiladi

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean accountLocked = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public boolean isAccountNonLocked() { return !accountLocked; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
```

## 2-yondashuv: Alohida adapter (tavsiya etiladi)

Entity va Spring Security'ni ajratish — clean architecture:

```java
// Entity — faqat DB
@Entity
public class User {
    private Long id;
    private String username;
    private String password;
    private String role;
    private boolean active;
    // + getter/setter
}

// Adapter — Spring Security uchun
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public String getUsername() { return user.getUsername(); }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override public boolean isEnabled() { return user.isActive(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    // Entity'ga kirish uchun
    public User getUser() { return user; }
    public Long getId() { return user.getId(); }
}
```

## UserDetailsService implementatsiyasi

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Foydalanuvchi topilmadi: " + username
            ));

        return new UserPrincipal(user);
        // yoki: return user;  (agar User implements UserDetails bo'lsa)
    }
}
```

## SecurityConfig'da ulash

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
        throws Exception {
        return config.getAuthenticationManager();
    }
}
```

## Hozirgi foydalanuvchiga kirish

```java
// Controller'da
@GetMapping("/me")
public ResponseEntity<UserResponse> getCurrentUser(
    @AuthenticationPrincipal UserPrincipal principal
) {
    return ResponseEntity.ok(userService.findById(principal.getId()));
}

// Service'da (SecurityContextHolder orqali)
public User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
    return principal.getUser();
}
```

## Ko'p rollar

```java
// Enum
public enum UserRole {
    USER, ADMIN, MODERATOR
}

// Ko'p rol uchun — Set<UserRole>
@Entity
public class User {
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = new HashSet<>();
}

// UserDetails'da
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        .collect(Collectors.toList());
}
```
