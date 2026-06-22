# Spring nima va nima uchun kerak?

Spring Framework — Java dasturlar uchun eng keng tarqalgan framework. Lekin uni "framework" deb aytish kifoya emas: Spring aslida sizning obyektlaringizni boshqaruvchi, ularni yaratuvchi va bir-biriga ulaydi.

## Muammo: Springsiz Java

Katta ilova yozsangiz, o'nlab, yuzlab klass bo'ladi. Har bir klass boshqa klasslarga bog'liq. Bularni o'zingiz boshqarishingiz kerak:

```java
// Springsiz — hamma narsani o'zingiz qilasiz
public class Main {
    public static void main(String[] args) {
        Database database = new Database("localhost", "5432");
        UserRepository userRepository = new UserRepository(database);
        EmailService emailService = new EmailService("smtp.gmail.com");
        UserService userService = new UserService(userRepository, emailService);

        // Agar Database konstruktori o'zgarsa — hamma joyda o'zgartirish kerak
        userService.registerUser("ali@example.com");
    }
}
```

Loyiha o'sgan sari bu yondashuv boshqarib bo'lmaydigan holatga keladi.

## Yechim: Spring

Spring ushbu mashaqqatni o'z zimmasiga oladi. Siz faqat "menga bu kerak" deysiz, Spring topib beradi:

```java
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        // Spring Database, UserRepository, EmailService, UserService ni
        // o'zi yaratadi, o'zi ulaydi
    }
}
```

![Spring Framework architecture](https://docs.spring.io/spring-framework/reference/_images/container-magic.png)

## Spring nima qiladi?

Spring sizning ilovangiz uchun uchta asosiy vazifani bajaradi:

**Birinchisi** — obyektlarni yaratadi. Siz `@Service`, `@Repository`, `@Component` yozsangiz yetarli, Spring bu klasslardan obyekt yasaydi.

**Ikkinchisi** — ularni bir-biriga ulaydi. `UserService` ga `UserRepository` kerakligini Spring tushunadi va o'z-o'zidan ulaydi.

**Uchinchisi** — hayot davrini boshqaradi. Qachon yaratilishi, qachon yo'q qilinishini Spring belgilaydi.

## Spring ekotizimi

Spring bitta kutubxona emas — bu bir oila:

```
Spring Core      — eng asosiy qism, IoC Container
Spring Boot      — Springni oson ishga tushirish
Spring MVC       — web ilovalar va REST API
Spring Data      — ma'lumotlar bazasi bilan ishlash
Spring Security  — autentifikatsiya va avtorizatsiya
Spring Cloud     — mikroservislar uchun
```

Bu repo `Spring Core` dan boshlanib, `Spring Security` gacha bo'lgan asosiy qismlarni qamrab oladi.
