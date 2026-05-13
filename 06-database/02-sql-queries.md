# SQL Queries - JOIN, Subquery, Index

## JOIN - Jadvallarni birlashtirish

JOIN ikki jadvalni bir-biriga ulash uchun ishlatiladi.

```sql
-- users jadvali
-- +----+-------+---------------------+
-- | id | name  | email               |
-- +----+-------+---------------------+
-- | 1  | Ali   | ali@example.com     |
-- | 2  | Vali  | vali@example.com    |
-- +----+-------+---------------------+

-- orders jadvali
-- +----+---------+--------+---------+
-- | id | product | amount | user_id |
-- +----+---------+--------+---------+
-- | 1  | Laptop  | 2000   | 1       |
-- | 2  | Mouse   | 50     | 1       |
-- | 3  | Keyboard| 100    | 2       |
-- +----+---------+--------+---------+
```

### INNER JOIN

Faqat ikkala jadvalda ham bor malumotlarni oladi.

```sql
SELECT u.name, o.product, o.amount
FROM users u
INNER JOIN orders o ON u.id = o.user_id;

-- Natija:
-- +------+---------+--------+
-- | name | product | amount |
-- +------+---------+--------+
-- | Ali  | Laptop  | 2000   |
-- | Ali  | Mouse   | 50     |
-- | Vali | Keyboard| 100    |
-- +------+---------+--------+
```

### LEFT JOIN

Users jadvalidagi hamma qatorlarni oladi, orders da bolmasa NULL.

```sql
SELECT u.name, o.product, o.amount
FROM users u
LEFT JOIN orders o ON u.id = o.user_id;

-- Natija: Agar 3-userning orderi bolmasa, NULL korsatiladi
```

### RIGHT JOIN

Orders jadvalidagi hamma qatorlarni oladi.

### FULL JOIN

Ikkala jadvalning hamma qatorlarini oladi.

## Subquery - Ichki so'rov

Bir SELECT ning ichida ikkinchi SELECT.

```sql
-- Eng katta order summasini topish
SELECT * FROM orders 
WHERE amount = (SELECT MAX(amount) FROM orders);

-- Eng kop order bergan foydalanuvchi
SELECT * FROM users 
WHERE id = (
    SELECT user_id 
    FROM orders 
    GROUP BY user_id 
    ORDER BY COUNT(*) DESC 
    LIMIT 1
);
```

## Index - Tezlikni oshirish

Index = jadvaldagi malumotlarni tez qidirish uchun maxsus tuzilma.

```sql
-- Index yaratish
CREATE INDEX idx_users_email ON users(email);

-- Unique index
CREATE UNIQUE INDEX idx_users_email_unique ON users(email);

-- Murakkab index (bir nechta ustun)
CREATE INDEX idx_users_name_email ON users(name, email);
```

### Index qachon kerak?

```sql
-- Index kerak (tez-tez qidiriladi):
SELECT * FROM users WHERE email = 'ali@example.com';
SELECT * FROM users WHERE name = 'Ali';

-- Index kerak emas (butun jadval oqiladi):
SELECT * FROM users;
```

### Index qachon zararli?

- Kop yozish (INSERT/UPDATE) boladigan jadvallarda
- Kichik jadvallarda (100 qatordan kam)
- Kam ishlatiladigan ustunlarda

## SQL da murakkab query lar

```java
@Query(value = """
    SELECT u.name, COUNT(o.id) as order_count, SUM(o.amount) as total_spent
    FROM users u
    LEFT JOIN orders o ON u.id = o.user_id
    WHERE u.is_active = true
    GROUP BY u.id, u.name
    HAVING COUNT(o.id) >= :minOrders
    ORDER BY total_spent DESC
    """, nativeQuery = true)
List<Object[]> findTopCustomers(@Param("minOrders") int minOrders);
```

## Xulosa

- JOIN -> jadvallarni ulash
- INNER JOIN -> ikkala jadvalda ham borlar
- LEFT JOIN -> birinchi jadvaldagi hammasi
- Subquery -> ichma-ich so'rovlar
- Index -> qidirishni tezlashtiradi
- INDEX -> SELECT ni tezlashtiradi, INSERT/UPDATE ni sekinlashtiradi
