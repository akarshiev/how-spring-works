# Flyway Migrations

Migration — ma'lumotlar bazasi sxemasidagi o'zgarishlarni versiyalash. Flyway shu o'zgarishlarni avtomatik, tartibli va takrorlanadigan qiladi.

## Nima uchun kerak?

Jamoada ishlayotganingizda:

- Developer A local DB'da `phone_number` ustun qo'shdi
- Developer B bu haqda bilmaydi — uning DB'si boshqacha
- Production'ga deploy qilganda — qaysi ustunlar bor, qaysi yo'q?

Flyway bilan hamma bir xil SQL fayllardan foydalanadi. Flyway qaysi migratsiyalar ishlaganini kuzatadi.

## Flyway qanday ishlaydi?

```
Ilova ishga tushadi
       |
       v
Flyway DB'ni tekshiradi: qaysi migratsiyalar ishlaganini (flyway_schema_history jadvalida)
       |
       v
Yangi migratsiyalar topilsa — ketma-ket ishlatadi
       |
       v
Ilovaga ruxsat beradi
```

```
flyway_schema_history:
+----+---------+-----------------------------+---------+
| id | version | description                 | success |
+----+---------+-----------------------------+---------+
| 1  | 1       | create users table          | true    |
| 2  | 2       | add phone number to users   | true    |
| 3  | 3       | create orders table         | true    |
+----+---------+-----------------------------+---------+
```

## Dependency

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<!-- PostgreSQL uchun qo'shimcha adapter (Flyway 10+) -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

## Fayl nomlash qoidasi

```
V{version}__{tavsif}.sql
   ↑              ↑
Versiya raqami  Ikki pastki chiziq, so'ngra tavsif

Misollar:
V1__create_users_table.sql
V2__add_phone_number_to_users.sql
V3__create_orders_table.sql
V10__add_user_roles.sql
```

Fayllar `src/main/resources/db/migration/` papkasiga joylashtiriladi.

## Misollar

```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    is_active   BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);
```

```sql
-- V2__create_orders_table.sql
CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id),
    total_amount DECIMAL(12, 2) NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status  ON orders(status);
```

```sql
-- V3__add_phone_to_users.sql
ALTER TABLE users
ADD COLUMN phone VARCHAR(20);
```

```sql
-- V4__add_full_text_search_index.sql
CREATE INDEX idx_users_name_fts ON users USING gin(to_tsvector('english', name));
```

## JPA DDL va Flyway — birga ishlatmang

```properties
# XATO — ikkalasi ham jadval boshqarishga harakat qiladi
spring.jpa.hibernate.ddl-auto=update  # Yomon!

# TO'G'RI — faqat Flyway boshqaradi
spring.jpa.hibernate.ddl-auto=validate  # JPA faqat mosligini tekshiradi
```

`validate` — Flyway yaratgan jadvallar JPA Entity'lari bilan mos kelishini tekshiradi. Mos kelmasa — ilova ishga tushmaydi.

## Test uchun alohida migratsiya

```
src/
  main/resources/db/migration/
    V1__create_tables.sql
    V2__seed_roles.sql
  test/resources/db/migration/
    V1__create_tables.sql   <- Bir xil schema
    V2__test_data.sql       <- Test ma'lumotlari
```

```properties
# application-test.properties
spring.flyway.locations=classpath:db/migration,classpath:db/testdata
```

## Flyway sozlamalari

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true    # Mavjud DB'ga birinchi marta qo'llaganda
spring.flyway.validate-on-migrate=true    # Migratsiyalarni tekshirish
spring.flyway.out-of-order=false          # Tartibsiz migratsiyaga yo'q deydi
```

## Rollback yo'q

Flyway Community versiyasida migratsiyani orqaga qaytarish yo'q. Yechim — yangi migratsiya yozish:

```sql
-- V5__rollback_phone_column.sql (agar V3'ni orqaga qaytarish kerak bo'lsa)
ALTER TABLE users DROP COLUMN IF EXISTS phone;
```

## Repeatable migratsiya (R__)

Versiyasiz, har o'zgarganda qayta ishlaydigan migratsiyalar:

```sql
-- R__create_views.sql — View yoki funksiyalar uchun
CREATE OR REPLACE VIEW active_users AS
SELECT id, name, email FROM users WHERE is_active = true;
```
