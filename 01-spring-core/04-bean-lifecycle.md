# Bean Lifecycle — Bean qanday tug'iladi va o'ladi?

Bean — Spring IoC Container boshqaradigan obyekt. Spring uni yaratadi, sozlaydi, va kerak bo'lmaganda yo'q qiladi.

## Hayot davri bosqichlari

```
Bean ta'riflanadi (@Component, @Bean)
         |
         v
Bean yaratiladi (konstruktor chaqiriladi)
         |
         v
Bog'liqliklar yuklanadi (DI)
         |
         v
Aware interfeyslari chaqiriladi
         |
         v
@PostConstruct metodi ishlaydi
         |
         v
Bean ishlatiladi
         |
         v
@PreDestroy metodi ishlaydi
         |
         v
Bean yo'q qilinadi
```

## @PostConstruct va @PreDestroy

Eng ko'p ishlatiladigan lifecycle annotation'lar:

```java
@Component
public class DatabaseConnectionPool {

    @PostConstruct  // Bean tayyor bo'lgandan keyin — bir marta ishlaydi
    public void initialize() {
        System.out.println("Ulanishlar ochilyapti...");
        // DB ga ulanishlarni yaratish
    }

    @PreDestroy     // Bean yo'q qilinishidan oldin — bir marta ishlaydi
    public void cleanup() {
        System.out.println("Ulanishlar yopilyapti...");
        // Barcha ulanishlarni yopish
    }
}
```

`@PostConstruct` qachon kerak? Ma'lumotlar bazasiga ulanish o'rnatish, kesh to'ldirish, konfiguratsiyani tekshirish — bir marta bajariladigan ishlar uchun.

## Lifecycle ni to'liq ko'rish

```java
@Component
public class LifecycleDemo {

    public LifecycleDemo() {
        System.out.println("1. Konstruktor: Bean yaratildi");
    }

    @Autowired
    public void setRepository(UserRepository repository) {
        System.out.println("2. DI: Bog'liqlik yuklandi");
    }

    @PostConstruct
    public void init() {
        System.out.println("3. @PostConstruct: Boshlang'ich sozlamalar");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("4. @PreDestroy: Bean o'chirilmoqda");
    }
}
```

Dastur ishga tushganda konsolda:
```
1. Konstruktor: Bean yaratildi
2. DI: Bog'liqlik yuklandi
3. @PostConstruct: Boshlang'ich sozlamalar
   ... dastur ishlaydi ...
4. @PreDestroy: Bean o'chirilmoqda
```

## Bean Scope — necha marta yaratiladi?

Scope — beanning "yashash doirasi". Default — `singleton`: butun ilova davomida faqat bitta obyekt.

```java
// Singleton (default) — bitta obyekt, hamma joyda shu
@Component
public class UserService { ... }

// Prototype — har safar yangi obyekt
@Component
@Scope("prototype")
public class ShoppingCart { ... }

// Request scope — har bir HTTP so'rov uchun yangi obyekt
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext { ... }
```

Qachon qaysi scope?

`singleton` — stateless, umumiy servislar uchun (`UserService`, `EmailService`). Spring da ko'pchilik beanlar singleton bo'lishi kerak.

`prototype` — har bir foydalanuvchi uchun alohida holat saqlanadigan holatda (`ShoppingCart`).

`request/session` — faqat web ilovalarda, har bir so'rov yoki sessiya uchun alohida holat kerak bo'lganda.

## InitializingBean va DisposableBean (eski usul)

```java
// Eski usul — hozir @PostConstruct/@PreDestroy ishlatiladi
@Component
public class LegacyBean implements InitializingBean, DisposableBean {

    @Override
    public void afterPropertiesSet() {
        // @PostConstruct ekvivalenti
    }

    @Override
    public void destroy() {
        // @PreDestroy ekvivalenti
    }
}
```

Bu interfeyslarni ko'rsangiz — bu eski Spring kodi. Yangi kodda `@PostConstruct` va `@PreDestroy` ishlating.
