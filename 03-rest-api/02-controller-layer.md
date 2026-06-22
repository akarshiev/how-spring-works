# Controller Layer — @RestController va @RequestMapping

Controller — HTTP so'rovlarni qabul qilib, javob qaytaradigan klass. MVC arxitekturasining "C" qismi.

## @RestController va @Controller farqi

```java
// @Controller — HTML sahifalar uchun (Thymeleaf, JSP)
@Controller
public class HtmlController {
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("user", currentUser);
        return "home";  // home.html shablonini qaytaradi
    }
}

// @RestController — REST API uchun (@Controller + @ResponseBody)
@RestController
public class ApiController {
    @GetMapping("/api/users")
    public List<User> getUsers() {
        return userService.findAll();  // Java ob'ektini JSON ga aylantiradi
    }
}
```

`@RestController` = `@Controller` + `@ResponseBody`. Bu degani, har bir metod qaytargan qiymat avtomatik JSON (yoki XML)ga serialise qilinadi.

## @RequestMapping — URL ni metodga bog'lash

```java
@RestController
@RequestMapping("/api/v1/users")  // Barcha metodlar shu bilan boshlanadi
public class UserController {

    @GetMapping            // GET /api/v1/users
    @GetMapping("/{id}")   // GET /api/v1/users/5
    @PostMapping           // POST /api/v1/users
    @PutMapping("/{id}")   // PUT /api/v1/users/5
    @DeleteMapping("/{id}") // DELETE /api/v1/users/5
}
```

## Ma'lumotlarni so'rovdan olish

### @PathVariable — URL'dan

```java
@GetMapping("/users/{userId}/orders/{orderId}")
public Order getOrder(
    @PathVariable Long userId,
    @PathVariable Long orderId
) {
    return orderService.findById(userId, orderId);
}
// GET /users/5/orders/10 -> userId=5, orderId=10
```

### @RequestParam — Query parametrdan

```java
@GetMapping("/users")
public Page<UserResponse> getUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(required = false) String search,
    @RequestParam(defaultValue = "name,asc") String sort
) {
    return userService.findAll(page, size, search, sort);
}
// GET /users?page=0&size=10&search=ali
```

### @RequestBody — JSON body'dan

```java
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
    @Valid @RequestBody CreateUserRequest request
) {
    UserResponse saved = userService.create(request);
    return ResponseEntity.status(201).body(saved);
}
```

### @RequestHeader — Headerdan

```java
@GetMapping("/users/me")
public UserResponse getCurrentUser(
    @RequestHeader("Authorization") String authHeader,
    @RequestHeader(value = "Accept-Language", defaultValue = "uz") String lang
) {
    // authHeader = "Bearer eyJhbGci..."
    return userService.getCurrentUser(authHeader, lang);
}
```

## To'liq controller namunasi

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(userService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
        // userService.findById xato tashlasa — @ControllerAdvice ushlaydi
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse saved = userService.create(request);
        URI location = URI.create("/api/v1/users/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## Controller qoidalari

Controller faqat so'rovni qabul qiladi va javob qaytaradi — business logic **yo'q**. Business logic `Service` qatlamida:

```java
// Yaxshi — Controller yupqa
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    return ResponseEntity.status(201).body(userService.create(request));
}

// Yomon — Controller'da business logic
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new RuntimeException("Email already exists");
    }
    User user = new User();
    user.setEmail(request.getEmail());
    // ... (bularning hammasi Service'da bo'lishi kerak)
    return ResponseEntity.status(201).body(userRepository.save(user));
}
```

API versiyalash ham controller darajasida hal qilinadi:

```java
@RequestMapping("/api/v1/users")  // Versiya URL'da
// yoki
@RequestMapping(value = "/api/users", headers = "API-Version=1")  // Headerda
```
