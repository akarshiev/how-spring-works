# Dependency Injection — Bog'liqliklarni yuklash

Dependency Injection (DI) — IoC Containerning asosiy quroli. "Bir klassga kerakli boshqa klassni tashqaridan berish" degani.

## Muammo: ichki yaratish

```java
// Yomon — Computer o'zi monitor yaratadi
public class Computer {
    private final Monitor monitor = new Monitor("Samsung");    // qattiq bog'liq
    private final Keyboard keyboard = new Keyboard("Logitech"); // qattiq bog'liq
}
```

Monitor markasini o'zgartirmoqchimisiz? `Computer` klassini o'zgartirishingiz kerak. Bu kodni qattiq bog'laydi va testlashni qiyinlashtiradi.

## Yechim: tashqaridan berish

```java
// Yaxshi — Computer tashqaridan oladi
public class Computer {
    private final Monitor monitor;
    private final Keyboard keyboard;

    public Computer(Monitor monitor, Keyboard keyboard) {
        this.monitor = monitor;
        this.keyboard = keyboard;
    }
}

// Endi istalgan monitor ulash mumkin
Computer pc = new Computer(new Monitor("LG"), new Keyboard("Apple"));
Computer laptop = new Computer(new Monitor("Dell"), new Keyboard("HP"));
```

Spring aynan shuni avtomatik qiladi.

## Uch usul

### Constructor Injection — tavsiya etiladigan usul

```java
@Service
public class UserService {
    private final UserRepository repository;
    private final EmailService emailService;

    // Spring bu konstruktorni topadi va repository, emailService ni beradi
    public UserService(UserRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }
}
```

Nima uchun eng yaxshi? `final` qilish mumkin (o'zgarmas), testlash oson, klass nima talab qilishini aniq ko'rsatadi.

### Field Injection — ishlatmang

```java
@Service
public class UserService {
    @Autowired  // Spring maydonni to'g'ridan-to'g'ri to'ldiradi
    private UserRepository repository;
}
```

Qulayligi — kam kod yoziladi. Muammosi — `final` qilib bo'lmaydi, testlashda qiyinchilik, klass nima kerakligi yashirin.

### Setter Injection — ixtiyoriy bog'liqliklar uchun

```java
@Service
public class NotificationService {
    private SmsService smsService;

    @Autowired(required = false)  // Bo'lmasa ham ishlayveradi
    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }
}
```

## Spring DI ni qanday bajaradi?

Spring Boot loyihasida faqat annotatsiya yetarli:

```java
@Repository  // 1. Spring: "Bu klassdan obyekt yaratayin"
public class UserRepository {
    public Optional<User> findById(Long id) { ... }
}

@Service     // 2. Spring: "Bu klassdan ham obyekt yaratayin"
public class UserService {
    private final UserRepository repository;

    // 3. Spring: "UserService ga UserRepository kerak ekan —
    //    IoC Containerdan topib, konstruktorda beraman"
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

Spring `UserService` ni yaratishda uning konstruktoriga `UserRepository` kerakligini tushunadi, Containerdan topib, uzatadi.

## @Qualifier — bir interfeys, ko'p implementatsiya

```java
public interface PaymentService {
    void pay(BigDecimal amount);
}

@Service("creditCard")
public class CreditCardService implements PaymentService { ... }

@Service("bankTransfer")
public class BankTransferService implements PaymentService { ... }

@Service
public class OrderService {
    private final PaymentService paymentService;

    // Qaysi implementatsiya kerakligini aniqlaymiz
    public OrderService(@Qualifier("creditCard") PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

## Xulosa

Constructor Injection ishlating — har doim. Field Injection (`@Autowired` maydoniga) ishlatmang. Spring DI ni avtomatik bajaradi: siz faqat annotatsiya qo'yasiz, qolganini Spring hal qiladi.
