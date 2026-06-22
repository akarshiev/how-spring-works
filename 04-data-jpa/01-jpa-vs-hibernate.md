# JPA vs Hibernate — Farqi nima?

Bu ikki atama tez-tez aralashtiriladi. Farqni bir marta tushunib olish kerak.

## JPA — standart

JPA (Jakarta Persistence API) — bu spetsifikatsiya. Qoidalar to'plami. Kod emas.

JPA Javada ma'lumotlar bazasi bilan ishlash uchun qanday annotation'lar bo'lishi, qanday interfeys bo'lishi kerakligini belgilaydi:

```java
// JPA qoidasi: Entity @Entity bilan belgilansin
@Entity
public class User { ... }

// JPA qoidasi: Primary key @Id bo'lsin
@Id
private Long id;

// JPA qoidasi: Ma'lumotlar olish uchun EntityManager bo'lsin
EntityManager em;
em.find(User.class, 1L);
```

JPA — faqat qoidalar. Uni bitta o'zi ishlatib bo'lmaydi.

## Hibernate — implementatsiya

Hibernate — JPA qoidalarini bajaradigan haqiqiy kod. JPA'ning eng mashhur implementatsiyasi.

```
JPA (interfeys/qoidalar)          → Hibernate (kod/implementatsiya)
javax.persistence.EntityManager   → org.hibernate.Session
@Entity, @Id, @OneToMany          → SQL ga aylantiradi
```

Analogiya: JPA = "Siz ma'lumotlar bazasi bilan ishlash uchun interfeys bo'lishi kerak" deydi. Hibernate = shu interfeysi amalga oshiradi.

## Spring Data JPA — uchinchi qatlam

Spring Data JPA — Hibernate ustiga qurilgan qatlam. JpaRepository kabi yordamchi mexanizmlar beradi:

```
Spring Data JPA      ← Siz buni ishlatasiz (JpaRepository)
       |
       v
JPA (standart)       ← Oraliq qoida qatlami
       |
       v
Hibernate            ← SQL yozadigan haqiqiy kod
       |
       v
PostgreSQL/MySQL     ← Ma'lumotlar bazasi
```

## Springsiz SQL — qiyinchilik

```java
// JPA'siz — qo'lda SQL
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setLong(1, userId);
ResultSet rs = stmt.executeQuery();

User user = new User();
user.setId(rs.getLong("id"));
user.setName(rs.getString("name"));
user.setEmail(rs.getString("email"));
// ... har bir ustun uchun
```

## Spring Data JPA bilan

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

// Ishlatish
User user = userRepository.findById(1L).orElseThrow();
```

Hibernate SQL ni o'zi yozdi: `SELECT u.* FROM users u WHERE u.id = ?`

## application.properties'da Hibernate

```properties
# Hibernate SQL ni konsolga chiqarish (development uchun)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Jadvallar bilan nima qilish kerak?
# validate — faqat mosligini tekshiradi (production)
# update  — yangi ustun/jadval qo'shadi (development)
# create  — har safar jadvalni o'chirib qayta yaratadi (test)
# none    — hech narsa qilmaydi
spring.jpa.hibernate.ddl-auto=validate

# PostgreSQL dialect
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## Xulosa

JPA — qoidalar (interfeys). Hibernate — qoidalarni bajaruvchi (kod). Spring Data JPA — Hibernate ustida qurilgan, `JpaRepository` kabi qulayliklar beradi. Amalda siz `@Entity`, `JpaRepository` yozasiz — Hibernate SQL ni yozadi.

Boshqa JPA implementatsiyalari ham bor (EclipseLink, OpenJPA), lekin Spring Boot'da Hibernate default.
