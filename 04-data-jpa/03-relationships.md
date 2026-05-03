# Relationships - Entity lar orasidagi bogliqlik

## Malumotlar bazasidagi bogliqliklar

Malumotlar bazasida 3 xil bogliqlik bor:

1. **OneToOne** -> Bir User -> bir Passport
2. **OneToMany** -> Bir User -> kop Order lar
3. **ManyToMany** -> Kop Student -> kop Course lar

## OneToOne - Birga-bir

Har bir User ning bitta pasporti bor. Har bir Passport bir User ga tegishli.

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @OneToOne(mappedBy = "user")  // "user" = Passport klassidagi maydon nomi
    private Passport passport;
}

@Entity
public class Passport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String passportNumber;
    
    @OneToOne
    @JoinColumn(name = "user_id")  // passports jadvalida "user_id" ustuni
    private User user;
}
```

Jadvallar:

```
users:        passports:
+----+------+    +----+-----------------+---------+
| id | name |    | id | passport_number | user_id |
+----+------+    +----+-----------------+---------+
| 1  | Ali  |    | 1  | AB1234567       | 1       |
+----+------+    +----+-----------------+---------+
```

## OneToMany / ManyToOne - Birga-kop

Bir User ning kop Order i bor. Har bir Order bir User ga tegishli.

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "user")  // "user" = Order klassidagi field
    private List<Order> orders = new ArrayList<>();
}

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String productName;
    private BigDecimal amount;
    
    @ManyToOne
    @JoinColumn(name = "user_id")  // orders jadvalida "user_id" ustuni
    private User user;
}
```

Jadvallar:

```
users:         orders:
+----+------+    +----+-------------+--------+---------+
| id | name |    | id | product     | amount | user_id |
+----+------+    +----+-------------+--------+---------+
| 1  | Ali  |    | 1  | Laptop      | 2000   | 1       |
+----+------+    | 2  | Mouse       | 50     | 1       |
                 +----+-------------+--------+---------+
```

## ManyToMany - Kopga-kop

Kop Student kop Course ga yozilishi mumkin.

```java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @ManyToMany
    @JoinTable(
        name = "student_courses",  // Uchinchi jadval
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();
}

@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @ManyToMany(mappedBy = "courses")
    private List<Student> students = new ArrayList<>();
}
```

Uchinchi jadval:

```
students:      courses:        student_courses (3-jadval):
+----+------+  +----+-------+  +------------+-----------+
| id | name |  | id | title |  | student_id | course_id |
+----+------+  +----+-------+  +------------+-----------+
| 1  | Ali  |  | 1  | Math  |  | 1          | 1         |
| 2  | Dana |  | 2  | Java  |  | 1          | 2         |
+----+------+  +----+-------+  | 2          | 1         |
                               +------------+-----------+
```

## Lazy vs Eager loading

```java
// LAZY - kerak paytida yuklanadi (tavsiya etiladi)
@OneToMany(fetch = FetchType.LAZY)  // Order lar faqat .getOrders() qilinganda yuklanadi
private List<Order> orders;

// EAGER - hamma vaqt yuklanadi (ehtiyot boling!)
@ManyToOne(fetch = FetchType.EAGER)  // User ni yuklaganda, order ham yuklanadi
private User user;
```

**Lazy** -> User ni olganda, Order lar yuklanmaydi. Order lar kerak bolganda yuklanadi.

**Eager** -> User ni olganda, Order lar ham yuklanadi.

**Tavsiya:** @OneToMany da LAZY, @ManyToOne da EAGER (default).

## Cascade - Birgalikda ozgartirish

Cascade = kaskad. Bir entity ni ozgartirsangiz, unga bogliq entity lar ham ozgaradi.

```java
@Entity
public class User {
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // CascadeType.ALL -> User bilan birga Order lar ham saqlansin, ochirilsin
    private List<Order> orders;
}
```

Cascade turlari:

| Cascade | Nima qiladi? |
|---------|-------------|
| ALL | Hamma operatsiyalar birga bajariladi |
| PERSIST | Birga saqlash |
| MERGE | Birga yangilash |
| REMOVE | Birga ochirish |
| REFRESH | Birga yangilash (DB dan qayta oqish) |
| DETACH | Birga ajratish |

## Orphan Removal - Yetimlarni ochirish

```java
@Entity
public class User {
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    // Agar Order User dan ajratilsa (order.setUser(null)), avtomatik ochirilsin
    private List<Order> orders;
}
```

## N+1 muammosiga e'tibor bering

```java
// N+1 muammosi: 1 ta query + N ta qoshimcha query
List<User> users = userRepository.findAll();  // 1 query
for (User user : users) {
    System.out.println(user.getOrders().size());  // N ta query (har bir user uchun)
}
```

Yechim: `@EntityGraph` yoki `JOIN FETCH`

```java
@Query("SELECT u FROM User u JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

## Xulosa

- @OneToOne -> 1:1 (User - Passport)
- @OneToMany / @ManyToOne -> 1:N (User - Orders)
- @ManyToMany -> N:N (Student - Courses)
- LAZY -> kerak paytida yuklash (tavsiya)
- Cascade -> birga ozgartirish
- N+1 muammosiga e'tibor bering
