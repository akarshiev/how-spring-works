# Dependency Injection (DI) - Bogliqliklarni yuklash

## Dependency Injection nima?

Dependency = Bogliqlik (bir obektning boshqa obektga muhtojligi)
Injection = Yuklash (kerakli obektni berib qoyish)

Ya'ni: **Bir obektga kerak bolgan boshqa obektni tashqaridan berish**.

## Real hayot misoli

Tasavvur qiling, siz kompyuter yig'yapsiz.

**Yomon usul** - hamma narsani oz ichiga yozish:

```java
// Kompyuter ozining monitorni oz yaratadi
public class Computer {
    private Monitor monitor = new Monitor();  // Oz monitor yaratadi
    private Keyboard keyboard = new Keyboard(); // Oz klaviatura yaratadi
}
```

Bu nima degani? Agar siz Monitor markasini ozgartirmoqchi bolsangiz, Computer klassini ozgartirishingiz kerak. Bu moslashuvchan emas.

**Yaxshi usul** - tashqaridan berish (Dependency Injection):

```java
// Kompyuter tashqaridan monitor va klaviatura oladi
public class Computer {
    private Monitor monitor;
    private Keyboard keyboard;
    
    // Constructor orqali berish
    public Computer(Monitor monitor, Keyboard keyboard) {
        this.monitor = monitor;
        this.keyboard = keyboard;
    }
}
```

Endi siz istalgan monitor va klaviatura ulashingiz mumkin.

## 3 xil DI usuli

### 1. Constructor Injection (Eng yaxshi usul)

```java
@Service
public class UserService {
    private final UserRepository repository;
    private final EmailService emailService;
    
    // Spring avtomatik ravishda repository va emailService ni topib beradi
    public UserService(UserRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }
}
```

**Afzalliklari:**
- Obekt yaratilganda hamma narsa tayyor boladi
- final qilish mumkin (ozgarmas)
- Test qilish oson

### 2. Field Injection (Qulay, lekin yaxshi emas)

```java
@Service
public class UserService {
    @Autowired  // Spring maydonga togridan-togri yuklaydi
    private UserRepository repository;
    
    @Autowired
    private EmailService emailService;
}
```

**Kamchiliklari:**
- final qilib bolmaydi
- Test qilish qiyin
- Classni yuklaganda nima kerakligi tushunarsiz

### 3. Setter Injection (Ixtiyoriy bogliqliklar uchun)

```java
@Service
public class UserService {
    private UserRepository repository;
    
    @Autowired  // Spring setter orqali yuklaydi
    public void setRepository(UserRepository repository) {
        this.repository = repository;
    }
}
```

**Qachon kerak?** - Agar bogliqlik ixtiyoriy bolsa (kerak bolmasa ham ishlayveradi).

## Qaysi usulni ishlatish kerak?

| Usul | Qachon ishlatiladi | Tavsiya |
|------|-------------------|---------|
| Constructor | Har doim (asosiy usul) | Eng yaxshi |
| Field | Tezda yozish kerak bolganda | Tavsiya etilmaydi |
| Setter | Ixtiyoriy bogliqliklar uchun | Kamdan-kam |

## Spring qanday DI qiladi?

```java
@Configuration
public class AppConfig {
    @Bean
    public UserRepository userRepository() {
        return new UserRepository();
    }
    
    @Bean
    public UserService userService() {
        // Spring constructor orqali DI qiladi
        return new UserService(userRepository());
    }
}
```

Yoki Spring Bootda annotationlar bilan:

```java
@Service  // Spring: "Bu klasni men boshqaraman"
public class UserService {
    private final UserRepository repository;
    
    // Spring: "Unga UserRepository kerak, topib beraman"
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

Spring `UserService` ni yaratish kerakligini koradi, unga `UserRepository` kerakligini tushunadi va IoC containerdan topib beradi.

## Xulosa

DI = Bir klassga kerakli narsalarni tashqaridan berish.

- Eng kop hollarda **Constructor Injection** ishlating
- Spring DI ni avtomatik qiladi
- DI kodni moslashuvchan va test qilish oson qiladi
