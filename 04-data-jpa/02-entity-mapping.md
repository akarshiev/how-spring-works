# Entity Mapping — @Entity, @Table, @Column

Entity — Java klassini ma'lumotlar bazasi jadvaliga bog'laydigan mexanizm. Har bir Entity dastur ishlaganda jadval sifatida namoyon bo'ladi.

## @Entity va @Table

```java
@Entity                          // Bu klass DB jadvaliga to'g'ri keladi
@Table(name = "users")           // Jadval nomi (default: klass nomi kichik harfda)
public class User {
    ...
}
```

Agar `@Table` bo'lmasa, Hibernate klass nomidan jadval nomini chiqaradi: `User` → `user`, `OrderItem` → `order_item`.

## @Id va ID generatsiyasi

Har bir Entity'da birlamchi kalit (`@Id`) bo'lishi shart:

```java
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY — PostgreSQL, MySQL da SERIAL/AUTO_INCREMENT
    private Long id;
}
```

Boshqa generatsiya strategiyalari:

```java
// SEQUENCE — PostgreSQL sekvens orqali (ishonchli va tez)
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
@SequenceGenerator(name = "user_seq", sequenceName = "user_id_seq", allocationSize = 50)
private Long id;

// UUID — tarqatilgan tizimlar uchun
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;

// MANUAL — qiymatni o'zingiz berasiz
@Id
private String id;  // Masalan: "USR-2024-001"
```

## @Column — ustun sozlamalari

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "phone_number", length = 20)
    private String phone;                           // nullable = true (default)

    @Column(name = "is_active", nullable = false)
    private boolean active = true;                  // Default qiymat

    @Column(name = "created_at", updatable = false) // Bir marta yoziladi
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")              // Uzun matn uchun
    private String bio;
}
```

## @Enumerated

```java
public enum UserRole {
    USER, ADMIN, MODERATOR
}

@Entity
public class User {
    @Enumerated(EnumType.STRING)   // "USER", "ADMIN" deb saqlanadi
    // @Enumerated(EnumType.ORDINAL) — 0, 1, 2 (xavfli — enum o'zgarsa buziladi)
    private UserRole role;
}
```

Har doim `EnumType.STRING` ishlating. `ORDINAL` — enum tartibiga bog'liq, bironta enum qo'shilsa yoki o'chirilsa ma'lumot buziladi.

## Sana va vaqt

Spring Boot 3.x va Jakarta EE 3+ da `LocalDateTime`, `LocalDate`, `Instant` to'g'ridan-to'g'ri mapplanadi:

```java
@Entity
public class User {
    private LocalDate birthDate;           // Faqat sana
    private LocalTime appointmentTime;     // Faqat vaqt
    private LocalDateTime createdAt;       // Sana + vaqt
    private Instant lastLoginAt;           // UTC timestamp (tavsiya etiladi)
    private ZonedDateTime eventAt;         // Timezone bilan
}
```

`@Temporal` — eski JPA 2.x annotatsiyasi, `java.util.Date` uchun kerak edi. Java 8 vaqt tiplarida shart emas.

## @Transient — saqlanmaydigan maydon

```java
@Entity
public class User {
    private String firstName;
    private String lastName;

    @Transient  // DB'ga saqlanmaydi, faqat memory'da
    private String fullName;

    @PostLoad  // DB'dan yuklanganda ishlaydi
    public void computeFullName() {
        this.fullName = firstName + " " + lastName;
    }
}
```

## @PrePersist va @PreUpdate

Saqlash va yangilashdan oldin avtomatik ishlaydigan metodlar:

```java
@Entity
public class User {
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist  // Birinchi saqlashdan oldin
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate   // Har bir yangilashdan oldin
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

Muqobil: `@EntityListeners(AuditingEntityListener.class)` + Spring Data Auditing.

## To'liq Entity namunasi

```java
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_created_at", columnList = "created_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // getter'lar
}
```
