# Spring Data Repository — JpaRepository

Spring Data JPA'ning asosiy qulayligi — siz faqat interfeys yozasiz, Spring implementatsiyani avtomatik yaratadi.

## JpaRepository

```java
// Bu 1 ta satr = 20+ tayyor metod
public interface UserRepository extends JpaRepository<User, Long> {
    // Bo'sh interfeys — hech narsa yozmasdan tayyor metodlar keladi
}
```

`JpaRepository<User, Long>` — `User` entity, `Long` primary key turi.

## Tayyor metodlar

```java
// CRUD
userRepository.save(user);              // Saqlash yoki yangilash
userRepository.findById(1L);            // Optional<User>
userRepository.findAll();               // Barchasi
userRepository.count();                 // Soni
userRepository.existsById(1L);          // Mavjudligi
userRepository.delete(user);            // O'chirish
userRepository.deleteById(1L);          // ID bo'yicha o'chirish
userRepository.deleteAll();             // Barchasini o'chirish

// Pagination va Sorting
userRepository.findAll(PageRequest.of(0, 20));
userRepository.findAll(Sort.by("name").ascending());
userRepository.findAll(PageRequest.of(0, 20, Sort.by("createdAt").descending()));
```

## Query metodlari — nom bo'yicha

Spring Data metod nomini o'qib, SQL yozadi:

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT * FROM users WHERE name = ? AND email = ?
    List<User> findByNameAndEmail(String name, String email);

    // SELECT * FROM users WHERE age > ?
    List<User> findByAgeGreaterThan(int age);

    // SELECT * FROM users WHERE age BETWEEN ? AND ?
    List<User> findByAgeBetween(int min, int max);

    // SELECT * FROM users WHERE name LIKE '%?%'
    List<User> findByNameContainingIgnoreCase(String keyword);

    // SELECT * FROM users WHERE active = true ORDER BY name ASC
    List<User> findByActiveTrueOrderByNameAsc();

    // SELECT * FROM users WHERE email = ? — true/false
    boolean existsByEmail(String email);

    // SELECT COUNT(*) FROM users WHERE active = ?
    long countByActive(boolean active);

    // DELETE FROM users WHERE email = ?
    @Transactional
    void deleteByEmail(String email);

    // SELECT * FROM users ORDER BY created_at DESC LIMIT 10
    List<User> findTop10ByOrderByCreatedAtDesc();
}
```

## @Query — o'zingiz SQL yozish

Metod nomi bilan ifodalash qiyin bo'lgan holatlarda:

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // JPQL — Entity nomi bilan
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.active = true")
    Optional<User> findActiveByEmail(@Param("email") String email);

    // JPQL — JOIN bilan
    @Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.id = :id")
    Optional<User> findByIdWithOrders(@Param("id") Long id);

    // Native SQL — jadval nomi bilan
    @Query(value = "SELECT * FROM users WHERE created_at > :date", nativeQuery = true)
    List<User> findRecentUsers(@Param("date") LocalDateTime date);

    // Pagination bilan
    @Query("SELECT u FROM User u WHERE u.active = true")
    Page<User> findAllActive(Pageable pageable);

    // UPDATE / DELETE — @Modifying shart
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.active = false WHERE u.lastLoginAt < :date")
    int deactivateInactiveUsers(@Param("date") LocalDateTime date);
}
```

## Pagination — sahifalash

```java
@Service
public class UserService {
    private final UserRepository repository;

    public Page<UserResponse> getUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(sortBy).ascending()
        );

        Page<User> userPage = repository.findAll(pageable);

        return userPage.map(user -> new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail()
        ));
    }
}
```

Controller'da:
```java
@GetMapping
public Page<UserResponse> getUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "createdAt") String sortBy
) {
    return userService.getUsers(page, size, sortBy);
}
```

Javob:
```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

## Projection — kerakli maydonlarni olish

Butun entity o'rniga faqat bir nechta maydon olish:

```java
// Interface projection
public interface UserSummary {
    Long getId();
    String getName();
    String getEmail();
}

public interface UserRepository extends JpaRepository<User, Long> {
    List<UserSummary> findAllProjectedBy();                  // Hamma foydalanuvchilar
    Optional<UserSummary> findProjectedById(Long id);        // Bitta
}
```

```java
// DTO projection (@Query bilan)
public record UserDto(Long id, String name, String email) {}

@Query("SELECT new com.example.dto.UserDto(u.id, u.name, u.email) FROM User u")
List<UserDto> findAllAsDtos();
```

## Service + Repository pattern

```java
@Repository  // Spring: "Bu bean malumotlar bazasi bilan ishlaydi"
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

@Service
@Transactional(readOnly = true)  // Default — faqat o'qish (tezroq)
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, UserMapper mapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserResponse> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("name")))
                         .map(mapper::toResponse);
    }

    public UserResponse findById(Long id) {
        return repository.findById(id)
                         .map(mapper::toResponse)
                         .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional  // O'zgartirish uchun alohida belgilash
    public UserResponse create(CreateUserRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        User user = mapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return mapper.toResponse(repository.save(user));
    }
}
```

Qoida: `Service` `@Transactional(readOnly = true)` bilan, o'zgartiruvchi metodlar `@Transactional` bilan.
