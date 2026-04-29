# Request va Response

## @RequestBody - So'rovni qabul qilish

Foydalanuvchi sizga JSON yuboradi -> Spring uni Java obektiga aylantiradi.

Foydalanuvchi yuborgan JSON:

```json
{
    "name": "Ali",
    "email": "ali@example.com",
    "age": 25
}
```

Sizning Java klassingiz:

```java
public class User {
    private String name;
    private String email;
    private int age;
    
    // getter va setter lar
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
```

Controllerga qabul qilish:

```java
@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody User user) {
    // Spring JSON ni avtomatik ravishda User obektiga aylantiradi
    System.out.println(user.getName());   // "Ali"
    System.out.println(user.getEmail());  // "ali@example.com"
    System.out.println(user.getAge());    // 25
    
    User saved = userService.save(user);
    return ResponseEntity.status(201).body(saved);
}
```

## @ResponseBody - Javobni yuborish

Spring Java obektni avtomatik ravishda JSON ga aylantiradi.

```java
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    // Spring User obektni JSON ga aylantiradi
    return userService.findById(id);
    // -> {"id": 1, "name": "Ali", "email": "ali@example.com", "age": 25}
}
```

## DTO (Data Transfer Object)

DTO = Malumotlarni tashish uchun obekt. Entity bilan Client orasida turgich vazifasini bajaradi.

### Nega DTO kerak?

Entity ichida malumotlar bor, lekin hammasini clientga yuborish xavfli:

```java
// Entity - malumotlar bazasidagi jadval
@Entity
public class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private String password;  // BUNI CLIENTGA YUBORIB BOLMAYDI!
    private boolean isAdmin;  // BU HAM XAVFLI!
}
```

DTO bilan:

```java
// DTO - clientga yuboriladigan malumot
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    
    // getter va setter lar
}
```

Va so'rov uchun:

```java
// CreateUserRequest - clientdan keladigan malumot
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;
    
    // getter va setter lar
}
```

Controllerda:

```java
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
    // 1. Request ni entity ga aylantirish
    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(request.getPassword());  // parolni shifrlash kerak!
    
    // 2. Saqlash
    User saved = userService.save(user);
    
    // 3. Entity ni DTO ga aylantirish
    UserResponse response = new UserResponse();
    response.setId(saved.getId());
    response.setName(saved.getName());
    response.setEmail(saved.getEmail());
    
    // 4. DTO ni qaytarish (parol va admin malumotlari yuborilmaydi)
    return ResponseEntity.status(201).body(response);
}
```

## Mapping - Entity va DTO ni aylantirish

### 1-usul: ModelMapper

```xml
<dependency>
    <groupId>org.modelmapper</groupId>
    <artifactId>modelmapper</artifactId>
</dependency>
```

```java
@Service
public class UserService {
    private final ModelMapper modelMapper = new ModelMapper();
    
    public UserResponse toDto(User user) {
        return modelMapper.map(user, UserResponse.class);
    }
    
    public User toEntity(CreateUserRequest request) {
        return modelMapper.map(request, User.class);
    }
}
```

### 2-usul: MapStruct (Tezroq)

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(User user);
    User toEntity(CreateUserRequest request);
}
```

## ResponseEntity - To'liq javob

ResponseEntity sizga javobni to'liq boshqarish imkonini beradi:

```java
@GetMapping("/users/{id}")
public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
    User user = userService.findById(id).orElse(null);
    
    if (user == null) {
        // 404 - topilmadi
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(null);
    }
    
    // 200 - muvaffaqiyatli
    UserResponse response = userMapper.toDto(user);
    return ResponseEntity
        .status(HttpStatus.OK)
        .header("Custom-Header", "value")
        .body(response);
}
```

## Jackson - JSON ni sozlash

```java
public class User {
    private Long id;
    
    @JsonIgnore  // Bu field JSON ga kiritilmaydi
    private String password;
    
    @JsonProperty("full_name")  // JSON da "full_name" deb ketadi
    private String name;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
```

## Xulosa

- @RequestBody -> JSON dan Java obektga
- @ResponseBody -> Java obektdan JSON ga
- DTO -> Client va Entity orasida himoya qatlami
- ResponseEntity -> Javobni to'liq boshqarish
- Jackson -> JSON formatini sozlash
