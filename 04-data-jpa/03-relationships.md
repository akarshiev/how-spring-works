# Relationships — Entity'lar orasidagi bog'liqlik

Ma'lumotlar bazasida jadvallar o'rtasidagi uchta asosiy bog'liqlik turi mavjud.

## @OneToOne — Birga-bir

Har bir foydalanuvchining bitta profili bor. Har bir profil bitta foydalanuvchiga tegishli.

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile profile;
}

@Entity
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bio;
    private String avatarUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

`mappedBy = "user"` — "boshqa tomonidagi `user` maydoni o'zaro bog'liqni boshqaradi" degani. `@JoinColumn` — chet kalit ustun shu jadvalda.

## @OneToMany / @ManyToOne — Birga-ko'p

Bitta foydalanuvchining ko'p buyurtmasi bor:

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();
}

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

DB'da faqat `orders.user_id` ustuni bor. `User.orders` — virtual bo'lib, SELECT orqali yuklanadi.

## @ManyToMany — Ko'pga-ko'p

Ko'p talabaning ko'p kurslarga yozilishi:

```java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
        name = "student_courses",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();
}

@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}
```

DB'da uchta jadval: `students`, `courses`, `student_courses` (oraliq jadval).

## Fetch Type — qachon yuklanadi?

```java
// LAZY — kerak bo'lganda yuklanadi (tavsiya)
@OneToMany(fetch = FetchType.LAZY)    // Default OneToMany'da
@ManyToMany(fetch = FetchType.LAZY)   // Default ManyToMany'da

// EAGER — har safar birgalikda yuklanadi (ehtiyot bo'ling!)
@ManyToOne(fetch = FetchType.EAGER)   // Default ManyToOne'da
@OneToOne(fetch = FetchType.EAGER)    // Default OneToOne'da
```

`EAGER` xavfli: `User` yuklanganda barcha `Order`'lari ham yuklanadi. 1000 ta user = 1000 ta qo'shimcha SELECT.

`@OneToMany` va `@ManyToMany`'da har doim `LAZY` qo'llang.

## N+1 muammosi va yechimi

```java
// MUAMMO: 1 SELECT users + N SELECT orders (har bir user uchun)
List<User> users = userRepository.findAll();  // 1 ta SQL
for (User user : users) {
    System.out.println(user.getOrders().size()); // N ta SQL!
}
```

Yechim — `JOIN FETCH`:

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.orders")
    List<User> findAllWithOrders();

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :id")
    Optional<User> findByIdWithOrders(@Param("id") Long id);
}
```

Yoki `@EntityGraph`:

```java
@EntityGraph(attributePaths = {"orders", "orders.items"})
@Query("SELECT u FROM User u WHERE u.active = true")
List<User> findAllActiveWithOrdersAndItems();
```

## Cascade — birgalikda o'zgartirish

```java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
// CascadeType.ALL = PERSIST + MERGE + REMOVE + REFRESH + DETACH

@OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
// Faqat saqlash va yangilashda kaskad

// + orphanRemoval: user'dan o'chirilgan order DB'dan ham o'chiriladi
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Order> orders;
```

## Ko'p uchraydigan xato: bidirectional bog'liqlik

```java
@Entity
public class User {
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    // Helper metod — ikkala tomonni ham sinxronlaydi
    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);  // Bu shart!
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setUser(null);
    }
}

// Ishlatish
User user = userRepository.findById(1L).orElseThrow();
Order order = new Order(BigDecimal.valueOf(100));
user.addOrder(order);  // Ikkala tomon to'g'ri
userRepository.save(user);
```

Faqat `user.getOrders().add(order)` desangiz — `order.user` null qoladi va DB'ga saqlanmaydi.
