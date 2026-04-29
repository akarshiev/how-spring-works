# Controller Layer - @RestController va @RequestMapping

## Controller nima?

Controller - bu HTTP so'rovlarni qabul qiluvchi klass. U Springga "agar /users ga GET kelsa, buni qil" deb aytadi.

## @RestController vs @Controller

```java
// Eski usul - har safar @ResponseBody yozish kerak
@Controller
public class OldController {
    @GetMapping("/hello")
    @ResponseBody  // Bunisiz HTML qaytaradi
    public String sayHello() {
        return "Hello";
    }
}

// Yangi usul - @RestController = @Controller + @ResponseBody
@RestController  // @ResponseBody oz-ozidan qoshiladi
public class NewController {
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello";  // JSON yoki matn qaytaradi
    }
}
```

**Qachon nima ishlatish kerak?**

- @RestController -> REST API (JSON qaytarish uchun)
- @Controller -> HTML sahifalar qaytarish uchun (Thymeleaf, JSP)

## @RequestMapping qanday ishlaydi?

@RequestMapping URL ni metodga bog'laydi.

```java
@RestController
@RequestMapping("/api/users")  // Barcha metodlar "/api/users" bilan boshlanadi
public class UserController {
    
    @GetMapping          // GET /api/users
    public List<User> getAll() { }
    
    @GetMapping("/{id}") // GET /api/users/5
    public User getById(@PathVariable Long id) { }
    
    @PostMapping         // POST /api/users
    public User create(@RequestBody User user) { }
    
    @PutMapping("/{id}") // PUT /api/users/5
    public User update(@PathVariable Long id, @RequestBody User user) { }
    
    @DeleteMapping("/{id}") // DELETE /api/users/5
    public void delete(@PathVariable Long id) { }
}
```

## @PathVariable - URL dan qiymat olish

```java
@GetMapping("/users/{userId}/orders/{orderId}")
public Order getOrder(
    @PathVariable Long userId,     // URL dan userId ni oladi
    @PathVariable Long orderId     // URL dan orderId ni oladi
) {
    return orderService.findById(userId, orderId);
}
```

So'rov: GET /users/5/orders/10
- userId = 5
- orderId = 10

## @RequestParam - Query parametrlarini olish

```java
@GetMapping("/users")
public List<User> getUsers(
    @RequestParam(defaultValue = "0") int page,     // ?page=0
    @RequestParam(defaultValue = "10") int size,     // ?size=10
    @RequestParam(required = false) String search   // ?search=john (ixtiyoriy)
) {
    return userService.findAll(page, size, search);
}
```

So'rov: GET /users?page=0&size=10&search=john

## @RequestHeader - Header dan qiymat olish

```java
@GetMapping("/users")
public List<User> getUsers(
    @RequestHeader("Authorization") String token,  // Headerdan token ni oladi
    @RequestHeader("Accept-Language") String lang   // Tilni oladi
) {
    return userService.findAll(token, lang);
}
```

## To'liq controller misol

```java
@RestController
@RequestMapping("/api/v1/users")  // Versiya bilan
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        List<User> users = userService.findAll(page, size);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userService.save(user);
        return ResponseEntity.status(201).body(saved);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## Xulosa

- @RestController -> JSON qaytaradi
- @RequestMapping -> URL ni metodga bog'laydi
- @PathVariable -> URL dan olish
- @RequestParam -> query parametrdan olish
- @RequestHeader -> headerdan olish
