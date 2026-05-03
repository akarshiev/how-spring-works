# JPQL va Native Query

## JPQL nima?

JPQL = Java Persistence Query Language. Bu JPA ning o'z query tili.

**JPQL vs SQL:**

- **SQL** -> jadval va ustun nomlari bilan ishlaydi: `SELECT * FROM users`
- **JPQL** -> Entity va field nomlari bilan ishlaydi: `SELECT u FROM User u`

## JPQL sintaksisi

### Oddiy SELECT

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // JPQL: SELECT * FROM users WHERE email = ?
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    
    // Hamma active userlar
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActiveUsers();
    
    // Ma'lum fieldlarni olish
    @Query("SELECT u.name, u.email FROM User u")
    List<Object[]> findNamesAndEmails();
}
```

### Parametrlar

```java
// 1-usul: nomli parametr (tavsiya etiladi)
@Query("SELECT u FROM User u WHERE u.name = :name AND u.age = :age")
List<User> findByNameAndAge(@Param("name") String name, @Param("age") int age);

// 2-usul: indeks parametr
@Query("SELECT u FROM User u WHERE u.name = ?1 AND u.age = ?2")
List<User> findByNameAndAge(String name, int age);
```

### LIKE

```java
@Query("SELECT u FROM User u WHERE u.name LIKE %:keyword%")
List<User> searchByName(@Param("keyword") String keyword);

@Query("SELECT u FROM User u WHERE u.email LIKE :domain%")
List<User> findByEmailDomain(@Param("domain") String domain);
```

### JOIN

```java
// INNER JOIN
@Query("SELECT u FROM User u JOIN u.orders o WHERE o.amount > :minAmount")
List<User> findUsersWithExpensiveOrders(@Param("minAmount") BigDecimal amount);

// LEFT JOIN
@Query("SELECT u FROM User u LEFT JOIN u.orders o WHERE o.id IS NULL")
List<User> findUsersWithoutOrders();

// JOIN FETCH (N+1 muammosini yechadi)
@Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.id = :id")
Optional<User> findByIdWithOrders(@Param("id") Long id);
```

### Aggregatsiya

```java
@Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
long countActiveUsers();

@Query("SELECT AVG(u.age) FROM User u")
double averageAge();

@Query("""
    SELECT u.name, COUNT(o.id) as orderCount
    FROM User u
    LEFT JOIN u.orders o
    GROUP BY u.name
    ORDER BY orderCount DESC
    """)
List<Object[]> findUserOrderCounts();
```

## Native SQL - Haqiqiy SQL

Native SQL da jadval nomlarini ishlatasiz, Entity nomlarini emas.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailNative(@Param("email") String email);
    
    // Murakkab query
    @Query(value = """
        SELECT u.*, COUNT(o.id) as total_orders
        FROM users u
        LEFT JOIN orders o ON u.id = o.user_id
        WHERE u.is_active = true
        GROUP BY u.id
        HAVING COUNT(o.id) > 5
        ORDER BY total_orders DESC
        LIMIT 10
        """, nativeQuery = true)
    List<User> findTopUsersByOrderCount();
}
```

**Qachon native SQL ishlatish kerak?**

- DB ga xos funksiyalar kerak bolsa (PostgreSQL -> JSONB, full-text search)
- Murakkab SQL optimizatsiyasi kerak bolsa
- Eski SQL ko'nikmalaringizni ishlatmoqchi bolsangiz

**Qachon JPQL ishlatish kerak?**

- Database mustaqil bolishi kerak bolsa (PostgreSQL dan MySQL ga otish mumkin)
- Entity nomlari bilan ishlash qulay bolsa
- O'qish uchun tushunarliroq bolsa

## Criteria API - Kod orqali query

Dinamik query lar uchun (filterlar ozgarib turadigan joylarda):

```java
@Service
public class UserService {
    @Autowired
    private EntityManager entityManager;
    
    public List<User> searchUsers(String name, Integer minAge, Boolean active) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (name != null) {
            predicates.add(cb.like(root.get("name"), "%" + name + "%"));
        }
        if (minAge != null) {
            predicates.add(cb.greaterThan(root.get("age"), minAge));
        }
        if (active != null) {
            predicates.add(cb.equal(root.get("isActive"), active));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("name")));
        
        return entityManager.createQuery(query).getResultList();
    }
}
```

## Projection - Faqat kerakli fieldlarni olish

```java
// DTO Projection
public interface UserSummary {
    String getName();
    String getEmail();
}

public interface UserRepository extends JpaRepository<User, Long> {
    // Faqat name va email qaytaradi
    List<UserSummary> findAllProjectedBy();
    
    @Query("SELECT u.name AS name, u.email AS email FROM User u WHERE u.isActive = true")
    List<UserSummary> findActiveUserSummaries();
}
```

## @NamedQuery - Query ga nom berish

```java
@Entity
@NamedQuery(
    name = "User.findByEmail",
    query = "SELECT u FROM User u WHERE u.email = :email"
)
public class User {
    ...
}
```

Ishlatish:

```java
@Repository
public class UserRepositoryImpl {
    @PersistenceContext
    private EntityManager em;
    
    public Optional<User> findByEmail(String email) {
        return em.createNamedQuery("User.findByEmail", User.class)
            .setParameter("email", email)
            .getResultStream()
            .findFirst();
    }
}
```

## Xulosa

- JPQL -> Entity va field nomlari bilan ishlaydi
- Native SQL -> jadval va ustun nomlari bilan ishlaydi
- JOIN FETCH -> N+1 muammosini yechadi
- Criteria API -> dinamik query lar
- Projection -> faqat kerakli fieldlar
- JPQL odatda yaxshiroq (database mustaqil), native SQL faqat maxsus holatlarda
