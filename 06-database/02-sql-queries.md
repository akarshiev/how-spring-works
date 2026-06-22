# SQL Queries — JOIN, Subquery, Index

## JOIN — jadvallarni birlashtirish

Ko'pgina hollarda ma'lumotlar bir nechta jadvalda bo'ladi. JOIN ularni birlashtirib so'rov qilish imkonini beradi.

```
users:                    orders:
+----+-------+            +----+---------+--------+---------+
| id | name  |            | id | product | amount | user_id |
+----+-------+            +----+---------+--------+---------+
| 1  | Ali   |            | 1  | Laptop  | 2000   | 1       |
| 2  | Vali  |            | 2  | Mouse   | 50     | 1       |
| 3  | Guli  |            | 3  | Keyboard| 100    | 2       |
+----+-------+            +----+---------+--------+---------+
```

### INNER JOIN — ikkalasida ham bor

```sql
SELECT u.name, o.product, o.amount
FROM users u
INNER JOIN orders o ON u.id = o.user_id;

-- Natija: Faqat buyurtmasi bor foydalanuvchilar
-- +------+---------+--------+
-- | name | product | amount |
-- +------+---------+--------+
-- | Ali  | Laptop  | 2000   |
-- | Ali  | Mouse   | 50     |
-- | Vali | Keyboard| 100    |
-- +------+---------+--------+
-- Guli yo'q — buyurtmasi yo'q
```

### LEFT JOIN — chap jadvalning barchasi

```sql
SELECT u.name, o.product, o.amount
FROM users u
LEFT JOIN orders o ON u.id = o.user_id;

-- Natija: Barcha foydalanuvchilar, buyurtmasi yo'q bo'lsa NULL
-- +------+---------+--------+
-- | name | product | amount |
-- +------+---------+--------+
-- | Ali  | Laptop  | 2000   |
-- | Ali  | Mouse   | 50     |
-- | Vali | Keyboard| 100    |
-- | Guli | NULL    | NULL   |  ← Buyurtmasi yo'q, lekin chiqadi
-- +------+---------+--------+
```

### Aggregatsiya bilan JOIN

```sql
SELECT
    u.name,
    COUNT(o.id) AS order_count,
    COALESCE(SUM(o.amount), 0) AS total_spent
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
GROUP BY u.id, u.name
ORDER BY total_spent DESC;
```

## Subquery — ichki so'rov

Bir `SELECT` ichida boshqa `SELECT`:

```sql
-- Eng katta buyurtma summasiga ega foydalanuvchi
SELECT name FROM users
WHERE id = (
    SELECT user_id FROM orders
    ORDER BY amount DESC
    LIMIT 1
);

-- O'rtacha sumadan ko'p xarid qilganlar
SELECT u.name, o.amount
FROM users u
JOIN orders o ON u.id = o.user_id
WHERE o.amount > (SELECT AVG(amount) FROM orders);

-- IN bilan subquery
SELECT * FROM users
WHERE id IN (
    SELECT DISTINCT user_id FROM orders WHERE amount > 1000
);
```

## CTE — Common Table Expression (WITH)

Murakkab so'rovlarni o'qilishi oson qiladi:

```sql
WITH monthly_revenue AS (
    SELECT
        DATE_TRUNC('month', created_at) AS month,
        SUM(amount) AS revenue
    FROM orders
    GROUP BY DATE_TRUNC('month', created_at)
),
top_customers AS (
    SELECT user_id, SUM(amount) AS total
    FROM orders
    WHERE created_at >= NOW() - INTERVAL '3 months'
    GROUP BY user_id
    HAVING SUM(amount) > 500
)
SELECT u.name, tc.total
FROM users u
JOIN top_customers tc ON u.id = tc.user_id
ORDER BY tc.total DESC;
```

## Index — qidiruvni tezlashtirish

Index — jadvaldagi ma'lumotlarni tez qidirish uchun maxsus tuzilma. Kitobdagi mundarija kabi ishlaydi.

```sql
-- Oddiy index
CREATE INDEX idx_users_email ON users(email);

-- Ko'p ustunli index
CREATE INDEX idx_orders_user_date ON orders(user_id, created_at);

-- Unikal index (UNIQUE constraint ham hosil qiladi)
CREATE UNIQUE INDEX idx_users_email_unique ON users(email);

-- Qisman index — faqat aktiv foydalanuvchilar
CREATE INDEX idx_users_active_email ON users(email) WHERE is_active = true;

-- Index ni o'chirish
DROP INDEX idx_users_email;
```

### Qachon index kerak?

```sql
-- FOYDALI — WHERE, JOIN, ORDER BY'da ko'p ishlatiladigan ustunlar
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- FOYDASIZ — kamdan-kam filtrlash uchun, kichik jadvallar uchun
-- Boolean ustunlar (SELECT * WHERE is_active — natija 50% qator)
```

### Index'ni tekshirish

```sql
-- Query'ning bajarilish rejasini ko'rish
EXPLAIN ANALYZE
SELECT * FROM users WHERE email = 'ali@example.com';

-- Natijada "Index Scan" bo'lsa — index ishlatilmoqda
-- "Seq Scan" bo'lsa — indeks yo'q yoki ishlatilmayapti
```

## PostgreSQL'ga xos imkoniyatlar

```sql
-- JSONB
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    attributes JSONB
);

INSERT INTO products VALUES (1, 'Noutbuk', '{"brand": "Dell", "ram": 16}');

SELECT * FROM products WHERE attributes->>'brand' = 'Dell';
SELECT * FROM products WHERE (attributes->>'ram')::int > 8;

-- Full-text search
SELECT * FROM users
WHERE to_tsvector('english', name || ' ' || email) @@ plainto_tsquery('ali example');

-- ARRAY
SELECT * FROM articles WHERE 'java' = ANY(tags);

-- Window function
SELECT name, amount,
    RANK() OVER (ORDER BY amount DESC) AS rank,
    SUM(amount) OVER () AS total
FROM orders;
```
