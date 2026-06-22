# Validation — Ma'lumotlarni tekshirish

Foydalanuvchi yuborgan ma'lumotlarni tekshirmasdan ishlatish — eng ko'p uchraydigan xavfsizlik xatosi. Bean Validation (Jakarta Validation) bu ishni annotatsiyalar orqali hal qiladi.

## Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## DTO'ga annotatsiya qo'shish

```java
public class CreateUserRequest {

    @NotBlank(message = "Ism kiritilishi shart")
    @Size(min = 2, max = 50, message = "Ism 2 dan 50 gacha belgidan iborat bo'lishi kerak")
    private String name;

    @NotBlank(message = "Email kiritilishi shart")
    @Email(message = "Email noto'g'ri formatda")
    private String email;

    @NotNull(message = "Yosh kiritilishi shart")
    @Min(value = 18, message = "Yosh 18 dan katta bo'lishi kerak")
    @Max(value = 120, message = "Yosh 120 dan kichik bo'lishi kerak")
    private Integer age;

    @Pattern(regexp = "^\\+998[0-9]{9}$", message = "Telefon noto'g'ri formatda (+998901234567)")
    private String phone;  // Ixtiyoriy — null bo'lsa @Pattern ishlamaydi

    @NotBlank
    @Size(min = 8, message = "Parol kamida 8 ta belgidan iborat bo'lishi kerak")
    private String password;
}
```

## Controller'da @Valid

```java
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
    @Valid @RequestBody CreateUserRequest request
    // @Valid bo'lmasa — annotatsiyalar tekshirilmaydi!
) {
    return ResponseEntity.status(201).body(userService.create(request));
}
```

Validation xatosi chiqsa — `MethodArgumentNotValidException` tashlanadi. `@ControllerAdvice`'da ushlang (oldingi bo'lim).

## Asosiy annotatsiyalar

| Annotatsiya | Nima tekshiradi? | Null'ni? |
|-------------|-----------------|----------|
| `@NotNull` | Null emas | Xato |
| `@NotEmpty` | Null emas va bo'sh emas | Xato |
| `@NotBlank` | Null emas, bo'sh emas, probel emas | Xato |
| `@Size(min, max)` | Uzunlik (String, Collection, Array) | O'tkazadi |
| `@Min(value)` | Minimal son | O'tkazadi |
| `@Max(value)` | Maksimal son | O'tkazadi |
| `@Email` | Email formati | O'tkazadi |
| `@Pattern(regexp)` | Regex pattern | O'tkazadi |
| `@Positive` | Musbat son (> 0) | O'tkazadi |
| `@PositiveOrZero` | Musbat yoki nol | O'tkazadi |
| `@Past` | O'tgan sana | O'tkazadi |
| `@Future` | Kelajak sana | O'tkazadi |

"O'tkazadi" — null qiymat uchun xato chiqarmaydi. Agar null bo'lmasligi kerak bo'lsa, `@NotNull` bilan birga ishlating.

## Custom Validator

Standart annotatsiyalar yetmasa, o'zingizniki yarating:

```java
// 1. Annotatsiya
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Bu email allaqachon ro'yxatdan o'tgan";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// 2. Validator klassi
@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) return true;  // null tekshirishni @NotNull qilsin
        return !userRepository.existsByEmail(email);
    }
}

// 3. Ishlatish
public class CreateUserRequest {
    @NotBlank
    @Email
    @UniqueEmail  // O'zingizning validatoringiz
    private String email;
}
```

## Guruhli validation

Yaratish va yangilashda turli qoidalar:

```java
// Guruhlar
public interface OnCreate {}
public interface OnUpdate {}

public class UserRequest {

    @Null(groups = OnCreate.class)      // Yaratishda id bo'lmasligi kerak
    @NotNull(groups = OnUpdate.class)   // Yangilashda id kerak
    private Long id;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 2, max = 50, groups = {OnCreate.class, OnUpdate.class})
    private String name;

    @NotBlank(groups = OnCreate.class)  // Yaratishda email majburiy
    @Email(groups = OnCreate.class)
    private String email;               // Yangilashda email o'zgartirilmaydi
}

// Controller'da
@PostMapping("/users")
public UserResponse create(
    @Validated(OnCreate.class) @RequestBody UserRequest request
) { ... }

@PutMapping("/users/{id}")
public UserResponse update(
    @PathVariable Long id,
    @Validated(OnUpdate.class) @RequestBody UserRequest request
) { ... }
```

## @Valid ichida ob'ekt

Ichki ob'ektni ham tekshirish uchun:

```java
public class OrderRequest {

    @NotNull
    @Valid  // AddressRequest ham tekshirilsin
    private AddressRequest deliveryAddress;
}

public class AddressRequest {
    @NotBlank
    private String city;

    @NotBlank
    private String street;
}
```

## Programmatic validation

Controller'dan tashqarida tekshirish kerak bo'lsa:

```java
@Service
public class UserService {

    @Autowired
    private Validator validator;

    public void validateAndCreate(CreateUserRequest request) {
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            Map<String, String> errors = violations.stream()
                .collect(Collectors.toMap(
                    v -> v.getPropertyPath().toString(),
                    ConstraintViolation::getMessage
                ));
            throw new ValidationException("Validatsiya xatosi", errors);
        }

        userRepository.save(mapToEntity(request));
    }
}
```
