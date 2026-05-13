# Flyway Migrations

## Migration nima?

Migration = malumotlar bazasi jadvallarini versiyalash.

Tasavvur qiling, siz 1-hafta users jadvalini yaratdingiz. 2-haftada users ga phone_number ustunini qoshmoqchisiz. 3-haftada esa address jadvalini yaratmoqchisiz.

Migration bu ozgarishlarni versiya boyicha boshqaradi:

```
V1__create_users.sql      -> 1-versiya: users jadvali
V2__add_phone_number.sql  -> 2-versiya: phone_number ustuni
V3__create_address.sql    -> 3-versiya: address jadvali
```

## Nega migration kerak?

Migration bolmasa:

- Developer A: local DB ga qolda ustun qoshadi
- Developer B: local DB ga qolda ustun qoshadi, lekin boshqa nom bilan
- Production: ustunlar ozgargani uchun app ishlamaydi

Migration bilan:

- Hammasi bir xil fayllarni ishlatadi
- Avtomatik ravishda ishlaydi
- Versiyalash bor (1, 2, 3, ...)

## Flyway qanday ishlaydi?

Flyway DB da `flyway_schema_history` jadvalini yaratadi va qaysi migration lar ishlaganini kuzatadi.

```
flyway_schema_history jadvali:
+----+---------+---------------------+---------+
| id | version | description         | success |
+----+---------+---------------------+---------+
| 1  | 1       | create users        | true    |
| 2  | 2       | add phone number    | true    |
| 3  | 3       | create address      | true    |
+----+---------+---------------------+---------+
```

## Spring Boot da Flyway

### 1-qadam: Dependency

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### 2-qadam: Migration fayllarini yaratish

src/main/resources/db/migration/ papkasiga joylashtiring:

```
src/main/resources/db/migration/
    V1__create_users.sql
    V2__add_phone_number.sql
    V3__create_orders_table.sql
```

### V1__create_users.sql

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    age INTEGER,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### V2__add_phone_number.sql

```sql
ALTER TABLE users 
ADD COLUMN phone_number VARCHAR(20);
```

### V3__create_orders_table.sql

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    product_name VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
```

## Flyway sozlamalari

```properties
# application.properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

## JPA ddl-auto vs Flyway

```properties
# Yomon: JPA ni migration bilan ishlatish
spring.jpa.hibernate.ddl-auto=update  # JPA jadvallarni yaratadi
# Flyway ham jadvallarni yaratadi -> ziddiyat!

# Yaxshi: Flyway ga ruxsat berish
spring.jpa.hibernate.ddl-auto=validate  # JPA faqat tekshiradi
# Flyway jadvallarni yaratadi va boshqaradi
```

## Rollback (qaytarish)

Flyway Community versiyasida rollback yoq. Faqat yangi migration yozish kerak.

```sql
-- V4__remove_phone_number.sql (agar V2 ni qaytarish kerak bolsa)
ALTER TABLE users DROP COLUMN phone_number;
```

## Xulosa

- Migration = DB ni versiyalash
- Flyway -> migration larni boshqaradi
- Fayllar: V{version}__{description}.sql
- src/main/resources/db/migration/ papkasiga qoyiladi
- JPA ddl-auto=update ishlatmang, Flyway ishlating
- Rollback yoq -> faqat yangi migration yozing
