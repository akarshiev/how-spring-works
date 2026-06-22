# JPQL va Native Query

## JPQL nima?

JPQL (Jakarta Persistence Query Language) — JPA'ning o'z so'rov tili. SQL'ga o'xshaydi, lekin jadval va ustun nomlari o'rniga Entity va maydon nomlari ishlatiladi.

```
SQL:   SELECT * FROM users WHERE email = 'ali@example.com'
JPQL:  SELECT u FROM User u WHERE u.email = 'ali@example.com'
         ↑            ↑
    Java ob'ekti   Java klassi nomi
```

Nima uchun JPQL? Ma'lumotlar bazasidan mustaqil — PostgreSQL'dan MySQL'ga o'tsangiz JPQL o'zgarmaydi.

## JPQL asosiy sintaksis

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // Barcha aktiv foydalanuvchilar
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findAllActive();

    // Parametr bilan — nomli (:name)
    @Query("SELECT u FROM User u WHERE u.name = :name AND u.age > :age")
    List<User> findByNameAndMinAge(@Param("name") String name, @Param("age") int age);

    // Parametr bilan — indeksli (?1)
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmailPositional(String email);

    // LIKE
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByName(@Param("keyword") String keyword);

    // COUNT
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActive();

    // Aggregatsiya
    @Query("SELECT AVG(u.age) FROM User u WHERE u.active = true")
    Double averageAgeOfActiveUsers();
}
```

## JOIN FETCH — N+1 muammosini yechish

```java
// MUAMMO: N+1 — orders alohida yuklanadi
@Query("SELECT u FROM User u WHERE u.active = true")
List<User> findActive();  // users SELECT + N ta orders SELECT

// YECHIM: JOIN FETCH — bitta so'rovda
@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.orders WHERE u.active = true")
List<User> findActiveWithOrders();  // Bitta SELECT

// Ichki ob'ektlar bilan
@Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN FETCH u.orders o
    LEFT JOIN FETCH o.items
    WHERE u.id = :id
    """)
Optional<User> findByIdWithOrdersAndItems(@Param("id") Long id);
```

## JPQL bilan aggregatsiya va guruhlash

```java
@Query("""
    SELECT u.name, COUNT(o.id), SUM(o.amount)
    FROM User u
    LEFT JOIN u.orders o
    WHERE u.active = true
    GROUP BY u.id, u.name
    HAVING COUNT(o.id) >= :minOrders
    ORDER BY SUM(o.amount) DESC
    """)
List<Object[]> findTopCustomers(@Param("minOrders") long minOrders);
```

`Object[]` bilan ishlash noqulay. DTO Projection yaxshiroq:

```java
public record CustomerStats(String name, Long orderCount, BigDecimal totalAmount) {}

@Query("""
    SELECT new com.example.dto.CustomerStats(u.name, COUNT(o.id), SUM(o.amount))
    FROM User u LEFT JOIN u.orders o
    GROUP BY u.id, u.name
    HAVING COUNT(o.id) >= :minOrders
    """)
List<CustomerStats> findTopCustomerStats(@Param("minOrders") long minOrders);
```

## Native SQL — haqiqiy SQL

Native SQL'da jadval va ustun nomlari ishlatiladi:

```java
// Oddiy native query
@Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
Optional<User> findByEmailNative(@Param("email") String email);

// PostgreSQL'ga xos funksiyalar
@Query(value = """
    SELECT *
    FROM users
    WHERE to_tsvector('english', name || ' ' || email) @@ plainto_tsquery(:query)
    """, nativeQuery = true)
List<User> fullTextSearch(@Param("query") String query);

// Murakkab JOIN
@Query(value = """
    SELECT u.*, COUNT(o.id) AS order_count
    FROM users u
    LEFT JOIN orders o ON u.id = o.user_id
    WHERE u.is_active = true
    GROUP BY u.id
    HAVING COUNT(o.id) > :minOrders
    ORDER BY order_count DESC
    LIMIT :limit
    """, nativeQuery = true)
List<Object[]> findTopUsers(@Param("minOrders") int minOrders, @Param("limit") int limit);
```

Native SQL'da pagination:

```java
@Query(
    value = "SELECT * FROM users WHERE active = true",
    countQuery = "SELECT COUNT(*) FROM users WHERE active = true",
    nativeQuery = true
)
Page<User> findAllActiveNative(Pageable pageable);
```

`countQuery` shart — Spring sahifalash uchun jami sonni bilishi kerak.

## Qachon nima ishlatish kerak?

**Query metodlari (metod nomi orqali)** — oddiy filtr va qidiruv uchun. `findByEmailAndActive`, `findTop10ByOrderByCreatedAtDesc`.

**JPQL** — murakkab filtrlar, JOIN'lar, aggregatsiya. Ma'lumotlar bazasidan mustaqil bo'lishi kerak bo'lganda.

**Native SQL** — DB'ga xos funksiyalar (`JSONB`, `tsvector`, `ARRAY`), juda murakkab optimizatsiya kerak bo'lganda, yoki mavjud SQL'ni ko'chirganingizda.

## Criteria API — dinamik query

Foydalanuvchi tanlaydigan filtrlar uchun (qidiruv formasi kabi):

```java
@Repository
public class UserSearchRepository {

    @PersistenceContext
    private EntityManager em;

    public List<User> search(String name, Integer minAge, Boolean active) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("name")),
                                   "%" + name.toLowerCase() + "%"));
        }
        if (minAge != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("age"), minAge));
        }
        if (active != null) {
            predicates.add(cb.equal(root.get("active"), active));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("createdAt")));

        return em.createQuery(query).getResultList();
    }
}
```

**Spring Data JPA Specification** — Criteria API'ni yanada qulaylashtiradi. Katta loyihalarda Criteria API o'rniga `JpaSpecificationExecutor` ishlating.
