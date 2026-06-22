# PostgreSQL asosiy tushunchalar

PostgreSQL — eng kuchli ochiq kodli relatsion ma'lumotlar bazasi. Spring Boot loyihalarida eng ko'p ishlatiladi.

## Ma'lumotlar bazasi nima?

Ma'lumotlarni tartibli, tez qidiriladigan va bir vaqtda ko'p foydalanuvchi bilan ishlay oladigan tizimda saqlash. Fayl yoki Excel bilan ishlash katta ma'lumotlar uchun ishlamaydi.

## Jadval (Table)

```
users jadvali
+----+-------+---------------------+-----+
| id | name  | email               | age |
+----+-------+---------------------+-----+
| 1  | Ali   | ali@example.com     | 25  |
| 2  | Vali  | vali@example.com    | 30  |
| 3  | Guli  | guli@example.com    | 22  |
+----+-------+---------------------+-----+

Jadval = tartibli ma'lumot to'plami
Ustun (Column) = ma'lumot turi: id, name, email, age
Qator (Row) = bitta yozuv
```

## Jadval yaratish

```sql
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,         -- Avtomatik o'sadigan ID
    name        VARCHAR(100) NOT NULL,          -- 100 belgigacha, majburiy
    email       VARCHAR(255) NOT NULL UNIQUE,   -- Takrorlanmasin, majburiy
    age         INTEGER CHECK (age >= 18),      -- 18 dan kichik bo'lmasin
    is_active   BOOLEAN NOT NULL DEFAULT true,  -- Default qiymat
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()  -- Vaqt + timezone
);
```

`BIGSERIAL` — PostgreSQL'ga xos, avtomatik ID generatsiya. `TIMESTAMPTZ` — timezone bilan vaqt (oddiy `TIMESTAMP` dan yaxshiroq).

## CRUD

```sql
-- INSERT
INSERT INTO users (name, email, age)
VALUES ('Ali', 'ali@example.com', 25);

-- SELECT — barcha
SELECT * FROM users;

-- SELECT — filter bilan
SELECT id, name, email FROM users WHERE age > 20 AND is_active = true;

-- SELECT — saralash va cheklash
SELECT * FROM users ORDER BY name ASC LIMIT 10 OFFSET 20;

-- UPDATE
UPDATE users
SET age = 26, name = 'Alisher'
WHERE id = 1;

-- DELETE
DELETE FROM users WHERE id = 3;

-- TRUNCATE — barchasini o'chirish (DELETE'dan tez)
TRUNCATE TABLE users;
```

## Ma'lumot turlari

| Tip | Nima uchun? |
|-----|-------------|
| `BIGSERIAL` / `BIGINT` | Primary key, ID |
| `VARCHAR(n)` | Qisqa matn (email, ism) |
| `TEXT` | Uzun matn (bio, tavsif) |
| `INTEGER` / `BIGINT` | Butun son |
| `DECIMAL(10,2)` | Pul miqdori |
| `BOOLEAN` | true/false |
| `TIMESTAMPTZ` | Sana + vaqt + timezone |
| `DATE` | Faqat sana |
| `UUID` | Universal unikal identifikator |
| `JSONB` | JSON ma'lumot (PostgreSQL'ga xos) |

## Spring Boot bilan ulash

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Docker bilan PostgreSQL

Development uchun Docker eng qulay yondashuv:

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

```bash
docker-compose up -d postgres
```

## Foydali psql buyruqlari

```bash
psql -U postgres -d mydb    # Ulanish

\dt                          # Jadvallarni ko'rish
\d users                     # Jadval tuzilishini ko'rish
\l                           # Ma'lumotlar bazalarini ko'rish
\q                           # Chiqish
```
