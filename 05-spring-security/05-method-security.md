# Method Security — @PreAuthorize, @Secured

URL darajasida himoya yetarli bo'lmagan hollarda — metod darajasida himoya ishlatiladi. `SecurityFilterChain` URL'ni himoyalaydi. `@PreAuthorize` konkret metodga kirish ruxsatini belgilaydi.

## Yoqish

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Method security ni yoqish
public class SecurityConfig { ... }
```

## @PreAuthorize — chaqirishdan oldin tekshirish

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    // Faqat ADMIN
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() { ... }

    // ADMIN yoki USER
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public UserResponse getUser(@PathVariable Long id) { ... }

    // Faqat o'z ma'lumotlarini yoki Admin ko'rishi mumkin
    @GetMapping("/{id}/profile")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public UserProfile getProfile(@PathVariable Long id) { ... }

    // Faqat login qilgan
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse getCurrentUser() { ... }

    // Hech kim kira olmasin
    @DeleteMapping("/all")
    @PreAuthorize("denyAll()")
    public void deleteAll() { ... }
}
```

## Service darajasida

Method security Controller'da emas, Service'da qo'yish — yanada mustahkam himoya:

```java
@Service
public class DocumentService {

    @PreAuthorize("hasRole('ADMIN')")
    public Document create(CreateDocumentRequest request) { ... }

    @PreAuthorize("@documentSecurity.canRead(#id, authentication)")
    public Document findById(Long id) { ... }

    @PreAuthorize("hasRole('ADMIN') or @documentSecurity.isOwner(#id, authentication)")
    public void delete(Long id) { ... }
}
```

`@documentSecurity` — Spring bean. `@` prefiksi bean nomini bildiradi:

```java
@Component("documentSecurity")
public class DocumentSecurityService {

    private final DocumentRepository documentRepository;

    public boolean canRead(Long documentId, Authentication auth) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) return false;
        if (doc.isPublic()) return true;

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return doc.getOwnerId().equals(principal.getId());
    }

    public boolean isOwner(Long documentId, Authentication auth) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) return false;

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return doc.getOwnerId().equals(principal.getId());
    }
}
```

## @PostAuthorize — bajarilgandan keyin tekshirish

Metod natijasini tekshirish uchun:

```java
@GetMapping("/{id}")
@PostAuthorize("returnObject.ownerId == authentication.principal.id or hasRole('ADMIN')")
public Document getDocument(@PathVariable Long id) {
    return documentService.findById(id);
    // Metod ishlaydi, lekin qaytgan ob'ekt tekshiriladi
    // Agar egasi emas va admin emas bo'lsa — AccessDeniedException
}
```

## @PreFilter va @PostFilter — to'plamlarni filtrlash

```java
// Kirayotgan ro'yxatni filtrlash
@PreFilter("filterObject.ownerId == authentication.principal.id")
public List<Document> archiveDocuments(List<Document> documents) {
    // Faqat o'ziga tegishli hujjatlar o'tadi
}

// Qaytayotgan ro'yxatni filtrlash
@PostFilter("filterObject.ownerId == authentication.principal.id or hasRole('ADMIN')")
public List<Document> findAll() {
    return documentRepository.findAll();
    // Hamma oladi, lekin faqat o'ziga tegishlilar qaytariladi
}
```

## @Secured — sodda usul

```java
@Secured("ROLE_ADMIN")  // Faqat bir rol
public void adminTask() { }

@Secured({"ROLE_USER", "ROLE_ADMIN"})  // Bir nechta rol
public void userTask() { }
```

`@PreAuthorize` nisbatan kam imkoniyatli — SpEL ifoda yo'q. Oddiy hollarda ishlatiladi.

## SpEL ifodalari

`@PreAuthorize` ichida Spring Expression Language:

```java
// Rol va authority
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('USER', 'MODERATOR')")
@PreAuthorize("hasAuthority('WRITE_DOCUMENTS')")

// Autentifikatsiya holati
@PreAuthorize("isAuthenticated()")
@PreAuthorize("isFullyAuthenticated()")  // Remember Me emas
@PreAuthorize("isAnonymous()")

// Parametr bilan
@PreAuthorize("#userId == authentication.principal.id")
public UserProfile getProfile(@P("userId") Long userId) { ... }

// Authentication ob'ektiga kirish
@PreAuthorize("authentication.principal.username == #username")

// Bean chaqirish
@PreAuthorize("@accessControl.hasAccess(#id, authentication)")

// Murakkab ifoda
@PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id and hasRole('USER'))")
```

## Xatolikni ushlash

Method security xatosi `AccessDeniedException` tashlayd — `@ControllerAdvice`'da ushlanadi:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(403, "Bu amalni bajarish uchun ruxsatingiz yo'q"));
    }
}
```
