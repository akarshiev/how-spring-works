# Exception Handling - Xatolarni boshqarish

## Nima muammo?

Springda xatolik sodir bolganda, server 500 xatolik qaytaradi va foydalanuvchi hech narsani tushunmaydi.

Yomon usul - har bir metodda try-catch:

```java
@GetMapping("/users/{id}")
public ResponseEntity<?> getUser(@PathVariable Long id) {
    try {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    } catch (UserNotFoundException e) {
        return ResponseEntity.status(404).body("User topilmadi");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Server xatosi");
    }
}
```

Har bir metodda try-catch yozish juda kop kod takrorlashga olib keladi.

## @ControllerAdvice - Markaziy xatolik boshqaruvchisi

@ControllerAdvice bilan hamma xatolikni bir joyda ushlaysiz:

```java
@ControllerAdvice  // "Men hamma controller larni xatolikdan himoya qilaman"
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)  // Qaysi xatolikni ushlash?
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)  // 404
            .body(ex.getMessage());
    }
    
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<String> handleInvalidData(InvalidDataException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)  // 400
            .body(ex.getMessage());
    }
    
    @ExceptionHandler(Exception.class)  // Ushlanmagan hamma xatoliklar
    public ResponseEntity<String> handleGeneral(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
            .body("Serverda kutilmagan xatolik yuz berdi");
    }
}
```

Endi controllerlar toza kodi:

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        // try-catch YO'Q! Agar xatolik chiqsa, @ControllerAdvice ushlaydi
        return userService.findById(id);
    }
    
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
}
```

## Maxsus xatolik klasslari yaratish

```java
// 404 xatolik
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User topilmadi, id: " + id);
    }
}

// 400 xatolik
public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String message) {
        super(message);
    }
}

// 409 xatolik
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Bu email allaqachon mavjud: " + email);
    }
}
```

Xatoliklarni ishlatish:

```java
@Service
public class UserService {
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));  // 404
    }
    
    public User save(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new InvalidDataException("Email bosh bolmasligi kerak");  // 400
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException(user.getEmail());  // 409
        }
        
        return userRepository.save(user);
    }
}
```

## Chiroyli xatolik javobi

```java
// Xatolik javobi uchun DTO
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private List<String> errors;
    
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    // getter va setter lar
}
```

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(404, ex.getMessage());
        return ResponseEntity.status(404).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        ErrorResponse error = new ErrorResponse(400, "Validation xatosi");
        
        // Validation xatoliklarini yig'ish
        error.setErrors(ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(field -> field.getField() + ": " + field.getDefaultMessage())
            .collect(Collectors.toList()));
        
        return ResponseEntity.status(400).body(error);
    }
}
```

## Validatsiya xatoliklari (tez-tez uchraydi)

Spring Validation dan keladigan xatoliklar:

```java
@PostMapping("/users")
public User createUser(@Valid @RequestBody CreateUserRequest request) {
    // @Valid -> agar xato bolsa, MethodArgumentNotValidException otadi
    return userService.save(request);
}
```

@ControllerAdvice ushlaydi:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    List<String> errors = new ArrayList<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
        errors.add(fieldError.getField() + " -> " + fieldError.getDefaultMessage());
    }
    return ResponseEntity.badRequest().body(new ErrorResponse(400, "Validation xatosi", errors));
}
```

Natija:

```json
{
    "status": 400,
    "message": "Validation xatosi",
    "timestamp": "2026-05-15T10:30:00",
    "errors": [
        "name -> Nom bosh bolmasligi kerak",
        "email -> Email notogri formatda",
        "age -> Yosh 0 dan katta bolishi kerak"
    ]
}
```

## Xulosa

- @ControllerAdvice -> markaziy xatolik boshqaruvchisi
- @ExceptionHandler -> qaysi xatolikni ushlashni aytadi
- Maxsus exception klasslari -> aniq xatolik turlari
- ErrorResponse -> chiroyli xatolik javobi
- Bir marta yoz, hamma joyda ishlat
