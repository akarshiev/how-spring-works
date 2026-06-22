# Request va Response — @RequestBody, @ResponseBody, DTO

Foydalanuvchi JSON yuboradi — Spring uni Java ob'ektiga aylantiradi. Spring Java ob'ektini — JSON ga aylantiradi. Bu jarayon Jackson kutubxonasi orqali ishlaydi.

## @RequestBody — JSON'dan Java ob'ektiga

```java
// Client yuboradi:
// POST /api/users
// {"name": "Ali", "email": "ali@example.com", "age": 25}

@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
    @RequestBody CreateUserRequest request  // Jackson JSON → Java
) {
    System.out.println(request.getName());   // "Ali"
    System.out.println(request.getEmail());  // "ali@example.com"
    System.out.println(request.getAge());    // 25
    ...
}
```

## DTO — nima va nima uchun?

DTO (Data Transfer Object) — Client va Entity o'rtasida turgich. Entity to'g'ridan-to'g'ri client'ga berilmaydi:

```java
// Entity — DB jadvalidagi barcha ma'lumotlar
@Entity
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;       // CLIENT'GA YUBORMANG!
    private String resetToken;     // XAVFLI!
    private boolean isAdmin;       // YASHIRIN!
    private LocalDateTime createdAt;
}
```

```java
// Response DTO — client'ga yuboriladigan ma'lumotlar
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    // password yo'q, resetToken yo'q, isAdmin yo'q
}

// Request DTO — client'dan keladigan ma'lumotlar
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;  // Bu esa kerak — user parol yuboradi
}

public class UpdateUserRequest {
    private String name;      // id yo'q — URL'dan keladi
    private String email;     // password yo'q — alohida endpoint
}
```

## DTO bilan ishlash

```java
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    // 1. Request DTO → Entity
    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    // 2. Saqlash
    User saved = userRepository.save(user);

    // 3. Entity → Response DTO
    UserResponse response = new UserResponse();
    response.setId(saved.getId());
    response.setName(saved.getName());
    response.setEmail(saved.getEmail());

    return ResponseEntity.status(201).body(response);
}
```

## Mapper — Entity va DTO o'rtasida

Qo'lda mapping — takroriy kod. Mapper kutubxonalar bu ishni avtomatlashtiradi.

**MapStruct** — compile time'da mapper generatsiya qiladi, eng tez:

```java
// pom.xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

// Mapper interfeysi
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(CreateUserRequest request);
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);
}

// Ishlatish
@Service
public class UserService {
    private final UserMapper userMapper;

    public UserResponse create(CreateUserRequest request) {
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toResponse(userRepository.save(user));
    }
}
```

## ResponseEntity — to'liq javob

`ResponseEntity` status kodi, headerlar va body'ni birgalikda belgilash imkonini beradi:

```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
    UserResponse user = userService.findById(id);
    return ResponseEntity
        .ok()                                    // 200 status
        .header("X-User-Id", id.toString())      // Custom header
        .body(user);                             // Body
}

@PostMapping
public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest req) {
    UserResponse saved = userService.create(req);
    return ResponseEntity
        .status(HttpStatus.CREATED)              // 201 status
        .location(URI.create("/api/users/" + saved.getId()))  // Location header
        .body(saved);
}
```

## Jackson — JSON ni sozlash

```java
public class UserResponse {
    private Long id;

    @JsonIgnore           // Bu field JSON'ga kirmaydi
    private String internalCode;

    @JsonProperty("full_name")  // JSON'da "full_name" deb chiqadi
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // Sana formati
    private LocalDateTime createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)  // null bo'lsa chiqarmaydi
    private String optionalField;
}
```

Global Jackson sozlamalari `application.properties`'da:

```properties
# null qiymatlarni JSON'ga kiritmaslik
spring.jackson.default-property-inclusion=non_null

# Sanani timestamp sifatida emas, string sifatida
spring.jackson.serialization.write-dates-as-timestamps=false

# CamelCase → snake_case (ixtiyoriy)
spring.jackson.property-naming-strategy=SNAKE_CASE
```

## Standart javob formati

Barcha javoblar bir xil formatda bo'lishi yaxshiroq:

```java
// Generic wrapper
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }
}

// Controller'da
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(userService.findById(id)));
}
```
