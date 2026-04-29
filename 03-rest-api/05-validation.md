# Validation - Malumotlarni tekshirish

## Nega validation kerak?

Foydalanuvchi hech qachon to'g'ri malumot yubormaydi. Shuning uchun malumotlarni tekshirish kerak.

Yomon usul - qo'lda tekshirish:

```java
@PostMapping("/users")
public ResponseEntity<?> createUser(@RequestBody User user) {
    // Qo'lda tekshirish
    if (user.getName() == null || user.getName().isBlank()) {
        return ResponseEntity.badRequest().body("Name bosh bolmasligi kerak");
    }
    if (user.getEmail() == null || !user.getEmail().contains("@")) {
        return ResponseEntity.badRequest().body("Email notogri");
    }
    if (user.getAge() != null && user.getAge() < 0) {
        return ResponseEntity.badRequest().body("Yosh manfiy bolmasligi kerak");
    }
    // ... va hokazo
}
```

Har bir metodda qo'lda tekshirish juda kop kod takrorlashga olib keladi.

## Bean Validation (@Valid)

Java da standart validation annotatsiyalari bor. Faqat @Valid qoshish kifoya.

### 1-qadam: Dependency qoshish

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 2-qadam: DTO ga annotatsiyalar qoshish

```java
public class CreateUserRequest {
    
    @NotBlank(message = "Name bosh bolmasligi kerak")
    @Size(min = 2, max = 50, message = "Name 2-50 belgi orasida bolishi kerak")
    private String name;
    
    @NotBlank(message = "Email bosh bolmasligi kerak")
    @Email(message = "Email notogri formatda")
    private String email;
    
    @NotNull(message = "Yosh kiritilishi shart")
    @Min(value = 0, message = "Yosh 0 dan kichik bolmasligi kerak")
    @Max(value = 150, message = "Yosh 150 dan katta bolmasligi kerak")
    private Integer age;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Telefon raqam notogri")
    private String phone;
    
    // getter va setter lar
}
```

### 3-qadam: Controllerda @Valid qoshish

```java
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
    @Valid @RequestBody CreateUserRequest request  // @Valid -> tekshir!
) {
    User saved = userService.save(request);
    return ResponseEntity.status(201).body(userMapper.toDto(saved));
}
```

Agar malumotlar xato bolsa -> Spring avtomatik ravishda 400 qaytaradi.

## Eng ko'p ishlatiladigan validation annotatsiyalari

| Annotatsiya | Nima tekshiradi? |
|-------------|-----------------|
| @NotBlank | String bosh emas va probel emas |
| @NotEmpty | Collection/Map bosh emas |
| @NotNull | null emas |
| @Size(min, max) | Uzunlik |
| @Min(value) | Minimum qiymat |
| @Max(value) | Maximum qiymat |
| @Email | Email format |
| @Pattern(regexp) | Regex pattern |
| @Positive | Musbat son |
| @PositiveOrZero | Musbat yoki 0 |
| @Past | Otgan sana |
| @Future | Kelajak sana |

## @Validated - Guruhli tekshirish

Har xil holatda har xil tekshirish:

```java
// Guruhlar
public class ValidationGroups {
    public interface Create {}    // Yaratish uchun
    public interface Update {}    // Yangilash uchun
}

public class UserRequest {
    @Null(groups = Create.class)      // Yaratishda id null bolishi kerak
    @NotNull(groups = Update.class)   // Yangilashda id kerak
    private Long id;
    
    @NotBlank(groups = Create.class)  // Yaratishda name majburiy
    @Size(min = 2, max = 50, groups = {Create.class, Update.class})
    private String name;
    
    @Email(groups = Create.class)     // Yaratishda email majburiy
    private String email;
}
```

Ishlatish:

```java
@PostMapping("/users")
public UserResponse createUser(
    @Validated(ValidationGroups.Create.class) @RequestBody UserRequest request
) { ... }

@PutMapping("/users/{id}")
public UserResponse updateUser(
    @Validated(ValidationGroups.Update.class) @RequestBody UserRequest request
) { ... }
```

## Custom validation

Agar standart annotatsiyalar yetmasa, ozingiz yaratasiz:

### 1-qadam: Annotatsiya yaratish

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Bu email allaqachon mavjud";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### 2-qadam: Validator klassini yaratish

```java
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) return true;  // @NotNull boshqa tekshiradi
        return !userRepository.existsByEmail(email);
    }
}
```

### 3-qadam: Ishlatish

```java
public class CreateUserRequest {
    @NotBlank
    @Email
    @UniqueEmail  // OZINGIZNING VALIDATORINGIZ!
    private String email;
}
```

## Xatolik javobini chiroyli qilish

```java
@ControllerAdvice
public class ValidationExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        
        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation xatosi",
            errors
        );
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

Natija:

```json
{
    "status": 400,
    "message": "Validation xatosi",
    "timestamp": "2026-05-20T14:30:00",
    "errors": {
        "email": "Email notogri formatda",
        "age": "Yosh 0 dan kichik bolmasligi kerak"
    }
}
```

## Xulosa

- @Valid -> bir soz bilan validatsiyani yoqish
- @NotBlank, @Email, @Size -> eng kop ishlatiladigan tekshirishlar
- Guruhli tekshirish -> turli holatda turli tekshirish
- Custom validation -> oz tekshirishingizni yozish
- @ControllerAdvice + @ExceptionHandler -> chiroyli xatolik javobi
