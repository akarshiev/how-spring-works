# UserDetails va UserDetailsService

## UserDetails nima?

UserDetails - Spring Security ning foydalanuvchi haqidagi interfeysi. Spring Security ozining foydalanuvchi modelini ishlatadi.

```java
// Spring Security ning interfeysi
public interface UserDetails {
    String getUsername();           // Foydalanuvchi nomi
    String getPassword();           // Parol (shifrlangan)
    Collection<? extends GrantedAuthority> getAuthorities();  // Rollar
    boolean isAccountNonExpired();  // Hisob muddati tugamaganmi?
    boolean isAccountNonLocked();   // Hisob bloklanmaganmi?
    boolean isCredentialsNonExpired(); // Parol muddati tugamaganmi?
    boolean isEnabled();            // Hisob aktivmi?
}
```

## UserDetailsService nima?

UserDetailsService - foydalanuvchini malumotlar bazasidan qidiradigan interfeys.

```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
```

## Implementatsiya qilish

### 1-qadam: User entity ga UserDetails ni implement qilish

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
    private Role role;
    
    private boolean enabled = true;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Rollarni GrantedAuthority ga aylantirish
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public boolean isAccountNonExpired() { return true; }
    
    @Override
    public boolean isAccountNonLocked() { return true; }
    
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    
    @Override
    public boolean isEnabled() { return enabled; }
    
    // getter va setter lar
}

public enum Role {
    USER,
    ADMIN
}
```

### 2-qadam: UserDetailsService yaratish

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) 
        throws UsernameNotFoundException {
        
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User topilmadi: " + username
            ));
    }
}
```

### 3-qadam: SecurityConfig ga qoshish

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .userDetailsService(userDetailsService);  // UserDetailsService ni sozlash
        
        return http.build();
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
}
```

## Entity ni UserDetails dan ajratish (yaxshiroq usul)

```java
// Entity - malumotlar bazasi uchun
@Entity
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private String role;
    private boolean enabled;
}

// Alohida UserDetails implementatsiyasi
public class SecurityUser implements UserDetails {
    private final User user;
    
    public SecurityUser(User user) {
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
    
    @Override
    public boolean isEnabled() { return user.isEnabled(); }
}
```

## Oson usul - UserDetails bilan yaratish

```java
@Service
public class UserService {
    
    public UserDetails createUser(String username, String password, String role) {
        // UserDetails ning oddiy implementatsiyasi
        return User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .roles(role)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build();
    }
}
```

## Xulosa

- UserDetails -> Spring Security ning foydalanuvchi modeli
- UserDetailsService -> foydalanuvchi qidiradigan service
- DaoAuthenticationProvider -> UserDetailsService ni ishlatadi
- PasswordEncoder -> parollarni shifrlash
- Entity ga UserDetails ni implement qilish yoki alohida klass yaratish mumkin
