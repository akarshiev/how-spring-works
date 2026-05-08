# Method Security - @PreAuthorize, @Secured

## Method Security nima?

URL larni emas, balki metodlarni himoya qilish.

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // BU METODNI FAQAT ADMIN ISHLATA OLSIN
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

URL /api/users/5 ga kirishda URL filteri ishlaydi, lekin @PreAuthorize metod darajasida ishlaydi.

## @EnableMethodSecurity

Avval bu annotation ni qoshish kerak:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Method Security ni yoqish
public class SecurityConfig {
    // ...
}
```

## @PreAuthorize - Metodni chaqirishdan oldin tekshirish

Eng kop ishlatiladigan annotation:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Faqat ADMIN
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnly() {
        return "Faqat adminlar kora oladi";
    }
    
    // ADMIN yoki USER
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String allUsers() {
        return "Hamma login qilganlar kora oladi";
    }
    
    // Faqat oz malumotlarini kora oladi
    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

## @PostAuthorize - Metod chaqirilgandan keyin tekshirish

```java
@GetMapping("/{id}")
@PostAuthorize("returnObject.owner == authentication.principal.username")
public Document getDocument(@PathVariable Long id) {
    // Metod ishlaydi, lekin natija tekshiriladi
    return documentService.findById(id);
    // Agar hujjat egasi hozirgi foydalanuvchi bolmasa -> AccessDeniedException
}
```

## @Secured - Eski usul

```java
@Secured("ROLE_ADMIN")  // Faqat ADMIN
public void adminTask() { }

@Secured({"ROLE_USER", "ROLE_ADMIN"})  // USER yoki ADMIN
public void userTask() { }
```

## @RolesAllowed - Java EE standarti

```java
@RolesAllowed("ADMIN")
public void adminTask() { }
```

## SpEL (Spring Expression Language)

@PreAuthorize ichida SpEL ishlatish mumkin:

```java
// SpEL ifodalari
@PreAuthorize("hasRole('ADMIN')")                          // Rol tekshirish
@PreAuthorize("hasAuthority('WRITE')")                     // Authority tekshirish
@PreAuthorize("#userId == authentication.principal.id")   // Parametrni tekshirish
@PreAuthorize("isAuthenticated()")                         // Login qilganmi?
@PreAuthorize("permitAll()")                              // Hamma kirishi mumkin
@PreAuthorize("denyAll()")                                // Hech kim kira olmaydi

// Murakkab ifodalar
@PreAuthorize("hasRole('ADMIN') or (#userId == principal.id)")
@PreAuthorize("hasRole('ADMIN') and isFullyAuthenticated()")
```

## Service darajasida himoya

```java
@Service
public class DocumentService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public Document create(Document document) {
        return repository.save(document);
    }
    
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public Document findById(Long id) {
        return repository.findById(id).orElseThrow();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
```

## Custom Permission Evaluator

Murakkab tekshirishlar uchun:

```java
@Component
public class DocumentPermissionEvaluator {
    
    public boolean hasAccess(Long documentId, Authentication auth) {
        // Murakkab logika
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) return false;
        return doc.getOwner().equals(auth.getName()) || 
               auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
```

## Xulosa

- @PreAuthorize -> metod chaqirilishidan oldin tekshiradi
- @PostAuthorize -> metod ishlagandan keyin tekshiradi
- @Secured -> eski usul
- SpEL -> murakkab tekshirishlar
- @EnableMethodSecurity -> birinchi qoshish kerak
- Service yoki Controller darajasida ishlatish mumkin
