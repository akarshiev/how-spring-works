# Entity Mapping - @Entity, @Table, @Column

## @Entity - Klassni jadvalga aylantirish

@Entity Springga "bu klass malumotlar bazasidagi jadvalga togri keladi" deb aytadi.

```java
@Entity  // Spring: "Bu klass jadvalga aylanadi"
public class User {
    @Id
    private Long id;
    private String name;
    private String email;
}
```

Bu Springga: "users jadvali yarat, unda id, name, email ustunlari bolsin" degani.

## @Table - Jadval nomini ozgartirish

Default: klass nomi = jadval nomi (`User` -> `user`)

```java
@Entity
@Table(name = "users")  // Jadval nomi "users" (avtomatik emas)
public class User {
    ...
}

// Yoki
@Entity
@Table(name = "app_users", schema = "public")  // Schema ham bersa boladi
public class User {
    ...
}
```

## @Id - Primary key

Har bir Entity da @Id bolishi shart:

```java
@Entity
public class User {
    @Id  // Bu primary key
    private Long id;
}
```

### ID yaratish strategiyalari

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // DB auto-increment
    private Long id;
}

// Yoki
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE)  // Sequence orqali
@SequenceGenerator(name = "user_seq", sequenceName = "user_sequence", allocationSize = 1)
private Long id;

// Yoki
@Id
@GeneratedValue(strategy = GenerationType.UUID)  // UUID (Hibernate 6+)
private String id;
```

## @Column - Ustunni sozlash

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "full_name", nullable = false, length = 100)
    private String name;
    
    @Column(unique = true, nullable = false)  // email unique bolishi kerak
    private String email;
    
    @Column(name = "user_age")  // Jadvalda "user_age" deb nomlanadi
    private Integer age;
    
    @Column(updatable = false)  // Bir marta yoziladi, ozgartirib bolmaydi
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "TEXT")  // TEXT tipida boladi
    private String bio;
}
```

## @Enumerated - Enum ni saqlash

```java
public enum Role {
    USER, ADMIN, MODERATOR
}

@Entity
public class User {
    @Enumerated(EnumType.STRING)  // "USER", "ADMIN" deb saqlanadi
    // @Enumerated(EnumType.ORDINAL) // 0, 1, 2 deb saqlanadi (tavsiya etilmaydi)
    private Role role;
}
```

## @Temporal - Sana va vaqt

```java
@Entity
public class User {
    @Temporal(TemporalType.DATE)       // Faqat sana (2026-05-15)
    private LocalDate birthDate;
    
    @Temporal(TemporalType.TIME)       // Faqat vaqt (14:30:00)
    private LocalTime appointmentTime;
    
    @Temporal(TemporalType.TIMESTAMP)  // Sana va vaqt (2026-05-15 14:30:00)
    private LocalDateTime createdAt;
}
```

## @Transient - Saqlanmaydigan maydon

```java
@Entity
public class User {
    private String firstName;
    private String lastName;
    
    @Transient  // Bu maydon jadvalda saqlanmaydi!
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
```

## To'liq misol

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "is_active")
    private boolean isActive;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;
    
    @PrePersist  // Saqlashdan oldin avtomatik ishlaydi
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate  // Yangilashdan oldin avtomatik ishlaydi
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // getter va setter lar
}
```

## DDL avtomatik yaratish

application.properties da:

```properties
spring.jpa.hibernate.ddl-auto=update
# create -> har safar jadvalni ochirib, qayta yaratadi
# update -> mayjud jadvalni yangilaydi (yangi ustun qoshadi)
# validate -> jadvalni tekshiradi, hech narsa ozgartirmaydi
# none -> hech narsa qilmaydi (production)

spring.jpa.show-sql=true  # SQL ni konsolga chiqaradi
```

## Xulosa

- @Entity -> klassni jadvalga aylantiradi
- @Table -> jadval nomini ozgartirish
- @Id -> primary key
- @Column -> ustun sozlamalari
- @Transient -> saqlanmaydigan maydon
- @Enumerated -> enum ni saqlash
- @Temporal -> sana va vaqt
- @PrePersist, @PreUpdate -> avtomatik ishlaydigan metodlar
