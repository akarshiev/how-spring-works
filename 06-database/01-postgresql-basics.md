# PostgreSQL asosiy tushunchalar

## PostgreSQL nima?

PostgreSQL - bu eng kuchli ochiq kodli malumotlar bazasi.

Oddiy qilib: PostgreSQL = malumotlarni saglaydigan va ularni qidirishga yordam beradigan dastur.

## Malumotlar bazasi nima?

Tasavvur qiling, sizda 1000 ta foydalanuvchi haqida malumot bor:

- Ism, email, telefon, manzil, yosh, ...

Bu malumotlarni Excelda saqlash mumkin, lekin:

- 1 million foydalanuvchi bolsa, Excel ochilmaydi
- Bir vaqtning ozida 100 kishi kirsa, Excel buziladi
- Malumotlarni qidirish sekin ishlaydi

Malumotlar bazasi aynan shu muammolarni yechadi.

## Jadval (Table)

Malumotlar jadvallarda saqlanadi:

```
users (jadval)
+----+-------+---------------------+-----+
| id | name  | email               | age |
+----+-------+---------------------+-----+
| 1  | Ali   | ali@example.com     | 25  |
| 2  | Vali  | vali@example.com    | 30  |
| 3  | Guli  | guli@example.com    | 22  |
+----+-------+---------------------+-----+
 |         |                      |      |
 id       name                   email  age
(ustun)   (ustun)                (ustun) (ustun)
```

- **Jadval** -> users (foydalanuvchilar)
- **Ustun (Column)** -> id, name, email, age
- **Qator (Row)** -> bitta foydalanuvchi

## PostgreSQL da jadval yaratish

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,       -- Avtomatik o'sadigan ID
    name VARCHAR(100) NOT NULL,     -- Ism (bosh bolmasligi kerak)
    email VARCHAR(255) UNIQUE,      -- Email (takrorlanmasligi kerak)
    age INTEGER CHECK (age > 0),    -- Yosh (0 dan katta)
    created_at TIMESTAMP DEFAULT NOW()  -- Yaratilgan vaqt
);
```

## CRUD - Asosiy operatsiyalar

### INSERT (yaratish)

```sql
INSERT INTO users (name, email, age) 
VALUES ('Ali', 'ali@example.com', 25);
```

### SELECT (oqish)

```sql
-- Hamma userlarni olish
SELECT * FROM users;

-- Faqat kerakli ustunlar
SELECT name, email FROM users;

-- Filter bilan
SELECT * FROM users WHERE age > 20;

-- Saralash bilan
SELECT * FROM users ORDER BY age DESC;

-- Cheklash bilan
SELECT * FROM users LIMIT 10;
```

### UPDATE (yangilash)

```sql
UPDATE users 
SET age = 26, name = 'Aliy' 
WHERE id = 1;
```

### DELETE (ochirish)

```sql
DELETE FROM users WHERE id = 3;
```

## Spring Boot da PostgreSQL

### application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=123
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

## Xulosa

- PostgreSQL -> kuchli, ochiq kodli malumotlar bazasi
- Jadval (Table) -> malumotlar saqlanadigan joy
- CRUD -> CREATE, READ, UPDATE, DELETE
- SQL -> malumotlar bazasi bilan gaplashish tili
- Spring Boot + PostgreSQL -> JPA orqali ishlaydi
