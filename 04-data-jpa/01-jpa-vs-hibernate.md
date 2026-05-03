# JPA vs Hibernate - Farqi nima?

## JPA nima?

JPA = Jakarta Persistence API (avvalgi Java Persistence API)

JPA - bu **standart** (qoidalar toplami). U faqat qoidalarni belgilaydi, ozi hech narsa qilmaydi.

JPA ning qoidalari:

```java
// JPA qoidasi: "Entity lar @Entity annotation bilan belgilansin"
@Entity
public class User { ... }

// JPA qoidasi: "Primary key @Id bilan belgilansin"
@Id
private Long id;

// JPA qoidasi: "Malumotlar bazasiga savol @Query bilan yozilsin"
@Query("SELECT u FROM User u WHERE u.email = ?1")
User findByEmail(String email);
```

JPA faqat qoidalar. Uni ishlatib bolmaydi.

## Hibernate nima?

Hibernate - bu **implementatsiya**. JPA qoidalarini bajaradigan haqiqiy kod.

JPA = qoidalar (interfeys)
Hibernate = kod (interfeysni implement qilgan klass)

```java
// JPA qoidasi (interfeys):
// javax.persistence.EntityManager

// Hibernate implementatsiyasi (haqiqiy kod):
// org.hibernate.internal.SessionImpl
```

## Analogiya

Tasavvur qiling, siz uy qurmoqchisiz.

- **JPA** = uy qurish qoidalari (poydevor, devor, tom bolishi kerak). Bu qogozdagi qoidalar.
- **Hibernate** = usta. Qoidalarni biladi va uyni quradi.

JPA yo'q, Hibernate yo'q -> SQL yozasiz:

```java
// Hech qanday framework yo'q - qo'lda SQL
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setLong(1, 5);
ResultSet rs = stmt.executeQuery();
User user = new User();
user.setId(rs.getLong("id"));
user.setName(rs.getString("name"));
```

JPA + Hibernate bilan:

```java
@Query("SELECT u FROM User u WHERE u.id = ?1")
User findById(Long id);
```

## JPA ning asosiy qismlari

| JPA standarti | Nima qiladi? |
|---------------|-------------|
| @Entity | Klassni malumotlar bazasi jadvaliga bog'laydi |
| @Id | Primary key ni belgilaydi |
| @Column | Jadval ustunini sozlaydi |
| @OneToMany | Bir-birga bogliqlik |
| EntityManager | Malumotlar bazasi bilan ishlash |
| JPQL | Java tilida SQL (JPA Query Language) |
| Criteria API | Kod orqali so'rov yozish |

## Hibernate ning qoshimcha imkoniyatlari

Hibernate JPA dan tashqari qoshimcha imkoniyatlarga ega:

- **Caching** - malumotlarni keshlash (1-darajali va 2-darajali kesh)
- **Lazy loading** - malumotlarni kerak paytda yuklash
- **Auto DDL** - jadvallarni avtomatik yaratish
- **Hibernate-specific annotations** - JPA da yo'q qoshimcha annotatsiyalar

## Spring Bootda qanday ishlaydi?

Spring Data JPA qoshilganda:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

Bu starter 3 ta narsani olib keladi:

1. **JPA** - standart (qoidalar)
2. **Hibernate** - JPA ni implement qiladi
3. **Spring Data JPA** - Springning JPA ga qoshimcha qulayliklari

```
Spring Data JPA
       |
       v
    JPA (standart qoidalar)
       |
       v
Hibernate (qoidalarni bajaruvchi)
       |
       v
 Malumotlar bazasi
```

## Xulosa

- JPA = qoidalar (interfeys)
- Hibernate = qoidalarni bajaruvchi (kod)
- Spring Data JPA = JPA ni oson ishlatish uchun qoshimcha
- Siz @Query va @Entity larni ishlatasiz -> Hibernate SQL ni yozadi
