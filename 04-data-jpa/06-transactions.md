# @Transactional — Tranzaksiyalar

Tranzaksiya — bir nechta ma'lumotlar bazasi operatsiyasini bir butun sifatida bajarish. Hammasi muvaffaqiyatli bo'lsa — saqlanadi (COMMIT). Biror joyda xato bo'lsa — barchasi bekor qilinadi (ROLLBACK).

## Nima uchun kerak?

Pul o'tkazmasini tasavvur qiling:

```
Ali hisobidan 100$ ayirish  →  (1-operatsiya)
Vali hisobiga 100$ qo'shish →  (2-operatsiya)
```

Agar birinchi operatsiya bajarildi, ikkinchisida xato chiqdi — Ali pulsiz, Vali pulsiz. Tranzaksiya bundan saqlayd:

```sql
BEGIN;
  UPDATE accounts SET balance = balance - 100 WHERE id = 1;  -- Ali
  UPDATE accounts SET balance = balance + 100 WHERE id = 2;  -- Vali
COMMIT;  -- Hammasi yaxshi
-- yoki
ROLLBACK;  -- Xato bo'lsa, hammasi avvalgi holatga
```

## @Transactional

```java
@Service
public class PaymentService {

    @Transactional  // Metod transaksiya ichida bajariladi
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId)
            .orElseThrow(() -> new AccountNotFoundException(fromId));
        Account to = accountRepository.findById(toId)
            .orElseThrow(() -> new AccountNotFoundException(toId));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Balans yetarli emas");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);
        // Xato yo'q → COMMIT. Xato chiqsa → ROLLBACK (ikkala saqlash ham bekor)
    }
}
```

Spring AOP orqali ishlaydi: `@Transactional` ko'rsa, metod atrofiga `BEGIN/COMMIT/ROLLBACK` qo'shadi.

## @Transactional(readOnly = true)

Faqat o'qish uchun transaksiyalar tezroq ishlaydi — Hibernate dirty checking qilmaydi:

```java
@Service
@Transactional(readOnly = true)  // Klass darajasida — default
public class UserService {

    public UserResponse findById(Long id) { ... }  // readOnly = true
    public Page<UserResponse> findAll(Pageable p) { ... }  // readOnly = true

    @Transactional  // O'zgartirish uchun alohida belgilash (readOnly = false)
    public UserResponse create(CreateUserRequest req) { ... }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest req) { ... }
}
```

## rollbackFor — qaysi xatolikda bekor qilish

Default: `RuntimeException` va `Error` da ROLLBACK, `Exception` da emas.

```java
// Checked exception da ham ROLLBACK
@Transactional(rollbackFor = Exception.class)
public void processPayment() throws IOException { ... }

// Muayyan xatolikda ROLLBACK qilmaslik
@Transactional(noRollbackFor = BusinessWarningException.class)
public void processOrder() { ... }
```

## Propagation — transaksiyalar o'rtasidagi muloqot

Bir `@Transactional` metod boshqasini chaqirganda nima bo'ladi?

```java
@Service
public class OrderService {

    @Transactional
    public void createOrder(Order order) {
        orderRepository.save(order);
        auditService.log("Order created");  // AuditService ham @Transactional
    }
}
```

Propagation turlari:

```java
// REQUIRED (default) — mavjud transaksiyaga qo'shiladi
@Transactional(propagation = Propagation.REQUIRED)

// REQUIRES_NEW — har doim yangi transaksiya (mustaqil)
@Transactional(propagation = Propagation.REQUIRES_NEW)
// OrderService rollback bo'lsa ham, AuditService yozadi

// SUPPORTS — transaksiya bo'lsa qo'shiladi, bo'lmasa transaksiyasiz
@Transactional(propagation = Propagation.SUPPORTS)

// NOT_SUPPORTED — transaksiyasiz ishlaydi (mavjudni to'xtatadi)
@Transactional(propagation = Propagation.NOT_SUPPORTED)

// MANDATORY — transaksiya bo'lishi shart (bo'lmasa xato)
@Transactional(propagation = Propagation.MANDATORY)

// NEVER — transaksiya bo'lmasligi shart (bo'lsa xato)
@Transactional(propagation = Propagation.NEVER)
```

Audit log uchun `REQUIRES_NEW` — asosiy transaksiya rollback bo'lsa ham, audit saqlanishi kerak:

```java
@Service
public class AuditService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action) {
        auditRepository.save(new AuditLog(action, LocalDateTime.now()));
    }
}
```

## Isolation — parallel transaksiyalar

Bir vaqtda ikkita transaksiya bir ma'lumotni o'qib/yozsa:

```java
@Transactional(isolation = Isolation.READ_COMMITTED)  // PostgreSQL default
public void processPayment() { ... }
```

| Isolation | Dirty Read | Non-repeatable Read | Phantom Read |
|-----------|-----------|---------------------|--------------|
| READ_UNCOMMITTED | Ha | Ha | Ha |
| READ_COMMITTED | Yo'q | Ha | Ha |
| REPEATABLE_READ | Yo'q | Yo'q | Ha |
| SERIALIZABLE | Yo'q | Yo'q | Yo'q |

Ko'pchilik holat uchun `READ_COMMITTED` (PostgreSQL default) yetarli.

## Cheklovlar

```java
@Service
public class UserService {

    // XATO 1: private metodda ishlamaydi — AOP proxy faqat public metodlarda
    @Transactional
    private void saveUser(User user) { ... }

    // XATO 2: shu klass ichidan chaqirish ishlamaydi — proxy chetlab o'tiladi
    public void doSomething() {
        this.createUser(...);  // @Transactional ishlamaydi!
    }

    @Transactional
    public void createUser(CreateUserRequest req) { ... }
}
```

Ichki chaqirish muammosiga yechim: Service'ni ikkita bo'limga ajratish yoki `ApplicationContext.getBean()` orqali o'zini olish (yaxshi emas) yoki `@TransactionalEventListener` ishlatish.

## @Version — Optimistic Lock

Bir vaqtda ikkita transaksiya bir Entity'ni yangilasa:

```java
@Entity
public class Account {

    @Id
    private Long id;

    @Version  // Hibernate avtomatik boshqaradi
    private Long version;

    private BigDecimal balance;
}

// Tranzaksiya 1: version=1, balance=100 → balance=50 saqlaydi, version=2
// Tranzaksiya 2: version=1, balance=100 → saqlashga harakat → VERSION NOTO'G'RI!
// → OptimisticLockException — klient qayta urinishi kerak
```

Bank, elektron savdo kabi parallelligi yuqori tizimlarda muhim.
