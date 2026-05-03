# Spring Data Repository - JpaRepository

## Repository nima?

Repository - malumotlar bazasiga savol yuboradigan klass. Spring Data JPA bilan siz faqat interfeys yozasiz, implementatsiyani Spring qiladi.

```java
// Bu 1 ta satr kod = 20 ta query metodi
public interface UserRepository extends JpaRepository<User, Long> {
    // Hech qanday kod yozish shart emas!
}
```

## JpaRepository qanday ishlaydi?

Spring Data JPA sizning interfeysingizni olib, uning implementatsiyasini avtomatik yaratadi.

```java
// Siz yozasiz:
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

// Spring avtomatik yaratadi:
// public class UserRepositoryImpl implements UserRepository {
//     public Optional<User> findByEmail(String email) {
//         return entityManager.createQuery(
//             "SELECT u FROM User u WHERE u.email = :email", User.class)
//             .setParameter("email", email)
//             .getResultStream()
//             .findFirst();
//     }
// }
```

## JpaRepository dan keladigan metodlar

JpaRepository ni extend qilsangiz, bu metodlar oz-ozidan keladi:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // === CRUD metodlari (CrudRepository dan) ===
    // save(entity) -> saqlash yoki yangilash
    // findById(id) -> Optional qaytaradi
    // findAll() -> hammasini olish
    // count() -> sonini olish
    // delete(entity) -> ochirish
    // deleteById(id) -> id boyicha ochirish
    // existsById(id) -> mavjudligini tekshirish
    
    // === Pagination (PagingAndSortingRepository dan) ===
    // findAll(Sort) -> saralash bilan
    // findAll(Pageable) -> sahifalash bilan
}
```

## Query metodlar - Nom orqali so'rov

Spring Data JPA metod nomini oqib, SQL yozadi.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
    
    // SELECT * FROM users WHERE name = ? AND email = ?
    Optional<User> findByNameAndEmail(String name, String email);
    
    // SELECT * FROM users WHERE name = ? OR email = ?
    List<User> findByNameOrEmail(String name, String email);
    
    // SELECT * FROM users WHERE age > ?
    List<User> findByAgeGreaterThan(int age);
    
    // SELECT * FROM users WHERE age BETWEEN ? AND ?
    List<User> findByAgeBetween(int min, int max);
    
    // SELECT * FROM users WHERE name LIKE '%value%'
    List<User> findByNameContaining(String keyword);
    
    // SELECT * FROM users WHERE email LIKE 'value%'
    List<User> findByEmailStartingWith(String prefix);
    
    // SELECT * FROM users ORDER BY name ASC
    List<User> findAllByOrderByNameAsc();
    
    // SELECT * FROM users WHERE active = true
    List<User> findByIsActiveTrue();
    
    // SELECT COUNT(*) FROM users WHERE email = ?
    boolean existsByEmail(String email);
    
    // SELECT COUNT(*) FROM users WHERE age > ?
    long countByAgeGreaterThan(int age);
    
    // DELETE FROM users WHERE email = ?
    void deleteByEmail(String email);
    
    // SELECT TOP 10 * FROM users ORDER BY created_at DESC
    List<User> findTop10ByOrderByCreatedAtDesc();
}
```

## @Query - JPQL va Native SQL

Agar metod nomi bilan ifodalash qiyin bolsa, ozingiz SQL yozasiz:

### JPQL (Java Persistence Query Language)

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // JPQL - Entity nomi bilan ishlaydi
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailJPQL(@Param("email") String email);
    
    // Murakkab query
    @Query("SELECT u FROM User u WHERE u.age > :minAge AND u.isActive = true ORDER BY u.name")
    List<User> findActiveUsersOlderThan(@Param("minAge") int age);
    
    // Faqat ma'lum ustunlarni olish
    @Query("SELECT u.name, u.email FROM User u WHERE u.isActive = true")
    List<Object[]> findActiveUserNamesAndEmails();
}
```

### Native SQL

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Native SQL - jadval nomi bilan ishlaydi
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailNative(@Param("email") String email);
    
    // Murakkab native query
    @Query(value = """
        SELECT u.*, COUNT(o.id) as order_count
        FROM users u
        LEFT JOIN orders o ON u.id = o.user_id
        GROUP BY u.id
        HAVING COUNT(o.id) > :minOrders
        """, nativeQuery = true)
    List<User> findUsersWithMinOrders(@Param("minOrders") int minOrders);
}
```

## Modifying - UPDATE va DELETE

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Modifying  // UPDATE yoki DELETE query lar uchun
    @Query("UPDATE User u SET u.isActive = false WHERE u.lastLoginAt < :date")
    int deactivateInactiveUsers(@Param("date") LocalDateTime date);
    
    @Modifying
    @Query("DELETE FROM User u WHERE u.email = :email")
    int deleteByEmail(@Param("email") String email);
}
```

## Pagination - Sahifalash

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Sahifalab olish
    Page<User> findAll(Pageable pageable);
    
    // Filter bilan sahifalash
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Page<User> findActiveUsers(Pageable pageable);
    
    // Saralash bilan
    List<User> findByIsActiveTrue(Sort sort);
}
```

Ishlatish:

```java
@Service
public class UserService {
    private final UserRepository repository;
    
    public Page<UserResponse> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<User> userPage = repository.findAll(pageable);
        
        return userPage.map(user -> userMapper.toDto(user));
    }
}
```

## Service + Repository birgalikda

```java
@Repository  // Spring: "Bu repository, malumotlar bazasi bilan ishlaydi"
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

@Service  // Spring: "Bu service, biznes logika"
public class UserService {
    private final UserRepository repository;
    
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
    
    @Transactional
    public UserResponse register(CreateUserRequest request) {
        // 1. Email mavjudligini tekshirish
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        
        // 2. Entity yaratish
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // 3. Saqlash
        User saved = repository.save(user);
        
        // 4. DTO ga aylantirish
        return userMapper.toDto(saved);
    }
}
```

## Xulosa

- JpaRepository -> extended qilsangiz, kop metodlar oz-ozidan keladi
- Query metodlar -> metod nomi orqali SQL yozish
- @Query -> ozingiz SQL yozish
- @Modifying -> UPDATE/DELETE uchun
- Pageable -> sahifalash
- Repository faqat malumotlar bazasi bilan ishlaydi, Service biznes logika bilan
