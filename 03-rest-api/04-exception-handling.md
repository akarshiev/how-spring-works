# Exception Handling — Xatolarni boshqarish

Har bir controller metodida try-catch yozish — kodni toza emas va takrorli qiladi. Spring'da barcha xatolarni bitta joyda ushlash imkoniyati bor.

## Muammo: har joyda try-catch

```java
// Yomon — takroriy kod
@GetMapping("/{id}")
public ResponseEntity<?> getUser(@PathVariable Long id) {
    try {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    } catch (UserNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", "Server xatosi"));
    }
}
```

## @ControllerAdvice — markaziy xatolik boshqaruvchisi

```java
@RestControllerAdvice  // @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DuplicateEmailException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(403, "Ruxsat yo'q"));
    }

    // Ushlanmagan barcha xatolar
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Log'ga yozing — lekin foydalanuvchiga tafsilot bermang
        log.error("Kutilmagan xato", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(500, "Serverda xatolik yuz berdi"));
    }
}
```

Endi controller'lar toza:

```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(userService.findById(id));
    // Xato chiqsa — @ControllerAdvice ushlaydi
}
```

## Custom Exception klasslari

```java
// 404
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Foydalanuvchi topilmadi, id: " + id);
    }
    public UserNotFoundException(String email) {
        super("Foydalanuvchi topilmadi: " + email);
    }
}

// 409
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Bu email allaqachon mavjud: " + email);
    }
}

// 400
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
```

## ErrorResponse — standart xatolik javobi

```java
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors;  // Validation xatoliklari uchun

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
```

## Validation xatolarini ushlash

`@Valid` bilan validation xatosi chiqganda `MethodArgumentNotValidException` tashlanadi:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new LinkedHashMap<>();

    ex.getBindingResult().getFieldErrors().forEach(error ->
        fieldErrors.put(error.getField(), error.getDefaultMessage())
    );

    ErrorResponse response = new ErrorResponse(400, "Validatsiya xatosi");
    response.setErrors(fieldErrors);

    return ResponseEntity.badRequest().body(response);
}
```

Natija:

```json
{
  "status": 400,
  "message": "Validatsiya xatosi",
  "timestamp": "2026-06-22T14:30:00",
  "errors": {
    "email": "Email noto'g'ri formatda",
    "name": "Ism 2 belgidan uzun bo'lishi kerak",
    "age": "Yosh 0 dan katta bo'lishi kerak"
  }
}
```

## Service da xatolik tashlash

```java
@Service
public class UserService {

    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        // UserNotFoundException tashlanadi → @ControllerAdvice ushlaydi → 404 qaytadi
    }

    public User create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        // ...
    }
}
```

## ProblemDetails — RFC 7807

Spring Boot 3.x'da standart xatolik formati:

```java
// application.properties
spring.mvc.problemdetails.enabled=true

// Endi xatolik javobi RFC 7807 formatida:
{
  "type": "https://example.com/errors/not-found",
  "title": "Not Found",
  "status": 404,
  "detail": "Foydalanuvchi topilmadi, id: 5",
  "instance": "/api/users/5"
}
```

`ProblemDetail` ni to'g'ridan-to'g'ri qaytarish:

```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ProblemDetail> handleNotFound(UserNotFoundException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Not Found");
    problem.setDetail(ex.getMessage());
    problem.setInstance(URI.create(request.getRequestURI()));
    return ResponseEntity.status(404).body(problem);
}
```
