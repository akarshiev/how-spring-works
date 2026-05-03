# @Transactional - Tranzaksiyalar

## Tranzaksiya nima?

Tranzaksiya = bir nechta operatsiyani bir butun qilib birlashtirish.

Real hayotda:

Pul otkazmasini tasavvur qiling:

```
Ali -> 100$ -> Vali
```

Bu ikki qadamdan iborat:

1. Alining hisobidan 100$ ayrilsin
2. Valining hisobiga 100$ qoshilsin

Agar 1-qadam boldi, 2-qadamda xatolik chiqsa, Ali pulsiz qoladi. Tranzaksiya bu muammoni yechadi.

Tranzaksiya bilan:

```
BEGIN;
  1. Alidan 100$ ayir
  2. Valiga 100$ qosh
COMMIT;  // Agar hammasi yaxshi bolsa

YOKI

ROLLBACK; // Agar xatolik chiqsa, hammasi avvalgi holatga qaytadi
```

## @Transactional qanday ishlaydi?

```java
@Service
public class PaymentService {
    
    @Transactional  // Bu metod ichidagi hammasi bir tranzaksiya
    public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();
        
        from.setBalance(from.getBalance().subtract(amount));
        accountRepository.save(from);
        
        // AGAR BU YERDA XATOLIK CHIQSA -> from qaytib o'zgaradi
        // (ROLLBACK boladi)
        
        to.setBalance(to.getBalance().add(amount));
        accountRepository.save(to);
        
        // Hammasi yaxshi -> COMMIT (haqiqiy saqlash)
    }
}
```

## @Transactional qoshilganda nima boladi?

Spring AOP orqali ishlaydi:

```java
// Siz yozasiz:
@Service
public class PaymentService {
    @Transactional
    public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
        // kod
    }
}

// Spring avtomatik wrapp qiladi:
// 1. beginTransaction()
// 2. transferMoney() ni chaqiradi
// 3. Agar xatolik bolsa -> rollback()
//    Agar xatolik bolmasa -> commit()
```

## @Transactional parametrlari

```java
@Service
public class UserService {
    
    // Faqat oqish uchun (tezroq ishlaydi)
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return repository.findById(id).orElseThrow();
    }
    
    // Xatolik vaqtida rollback
    @Transactional(rollbackFor = Exception.class)  // Hamma xatolikda rollback
    @Transactional(noRollbackFor = IllegalArgumentException.class)  // Bu xatolikda rollback qilma
    public void createUser(User user) {
        repository.save(user);
    }
    
    // Timeout - 5 sekunddan oshsa xatolik
    @Transactional(timeout = 5)
    public void longOperation() {
        // 5 sekunddan oshsa, TransactionTimedOutException
    }
}
```

## Isolation level - Izolyatsiya

Bir vaqtning ozida ikki foydalanuvchi bir malumotga ishlov bersa nima boladi?

```java
@Transactional(isolation = Isolation.READ_COMMITTED)  // PostgreSQL default
public void updateBalance(Long accountId, BigDecimal amount) {
    // ...
}
```

| Isolation | Dirty Read | Non-repeatable Read | Phantom Read |
|-----------|-----------|-------------------|-------------|
| READ_UNCOMMITTED | Ha | Ha | Ha |
| READ_COMMITTED | Yoq | Ha | Ha |
| REPEATABLE_READ | Yoq | Yoq | Ha |
| SERIALIZABLE | Yoq | Yoq | Yoq |

**READ_COMMITTED -> eng kop ishlatiladigan (PostgreSQL default)**

## Propagation - Tranzaksiya tarqalishi

Bir @Transactional metod ikkinchi @Transactional metodni chaqirsa:

```java
@Service
public class OrderService {
    
    @Transactional
    public void createOrder(Order order) {
        // ...
        paymentService.processPayment(order);  // Bu metod ham @Transactional
        // ...
    }
}
```

| Propagation | Nima qiladi? |
|-------------|-------------|
| REQUIRED (default) | Mavjud tranzaksiya ishlatiladi, yoq bolsa yangisi yaratiladi |
| REQUIRES_NEW | Har doim yangi tranzaksiya |
| NESTED | Agar mavjud bolsa, ichki tranzaksiya |
| NEVER | Tranzaksiya bolmasligi kerak |
| NOT_SUPPORTED | Tranzaksiya bolmasligi kerak, mavjud bolsa to'xtatiladi |
| MANDATORY | Tranzaksiya mavjud bolishi kerak |
| SUPPORTS | Tranzaksiya mavjud bolsa ishlatiladi, bolmasa ham ishlaydi |

```java
@Service
public class AuditService {
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // Har doim YANGI tranzaksiya
    // Asosiy tranzaksiya rollback bolsa ham, audit log saqlanadi
    public void log(String action, String details) {
        auditRepository.save(new AuditLog(action, details));
    }
}
```

## @Transactional cheklovlari

```java
@Service
public class UserService {
    
    // 1. XATOLIK! private metodda ishlamaydi
    @Transactional
    private void saveUser(User user) {
        // AOP proxy faqat public metodlarda ishlaydi
    }
    
    // 2. XATOLIK! Bir klass ichida chaqirilsa ishlamaydi
    public void doSomething() {
        saveUser(new User());  // @Transactional ishlamaydi!
    }
    
    @Transactional
    public void saveUser(User user) {
        repository.save(user);
    }
}
```

## Optimistic va Pessimistic Lock

### Optimistic Lock

```java
@Entity
public class Account {
    @Version  // Avtomatik boshqariladigan versiya raqami
    private Long version;
    
    private BigDecimal balance;
}

// Bir vaqtning o'zida 2 foydalanuvchi balance ni o'zgartirmoqchi
// -> OptimisticLockException
```

### Pessimistic Lock

```java
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)  // Jadvalni blokirovka qiladi
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);
}
```

## Xulosa

- @Transactional -> bir nechta operatsiyani bir butun qiladi
- Muvaffaqiyat -> COMMIT
- Xatolik -> ROLLBACK (hammasi avvalgi holatga qaytadi)
- readOnly = true -> oqish uchun (tezroq)
- rollbackFor -> qaysi xatolikda rollback qilishni belgilaydi
- Isolation -> bir vaqtda kirishni boshqarish
- Propagation -> tranzaksiyalar orasidagi muloqot
- @Version -> optimistic lock
