# Spring nima va nima uchun kerak?

## Oddiy sozlar bilan

Tasavvur qiling, siz uy qurayapsiz. Uy qurish uchun sizga:
- Gisht kerak
- Sement kerak
- Temir kerak
- Quvurlar kerak

Lekin bu materiallarning hammasini olib, uy qura olmaysiz. Sizga usta kerak. Spring aynan shu "usta" vazifasini bajaradi.

Spring kerak bolishiga sabab - Java dasturlarida obektlar (materiallar) kop boladi va ularni boshqarish qiyin. Spring bu obektlarni boshqaradi.

## Spring nima?

Spring - bu Java uchun framework. "Framework" deganda qorqishingiz shart emas. Oddiy qilib aytganda, Spring - bu sizning kodingizni ishga tushirish, obektlarni yaratish va ularni bir-biriga ulash uchun yordamchi dastur.

## Springsiz hayot

Springsiz Java dasturida obekt yaratish:

```java
// Springsiz - hamma narsani ozingiz qilasiz
public class Main {
    public static void main(String[] args) {
        // Har bir obektni ozingiz yaratasiz
        Database database = new Database();
        UserService userService = new UserService(database);
        EmailService emailService = new EmailService();
        
        // Hamma ulanishlarni ozingiz qilasiz
        userService.setEmailService(emailService);
        
        // Endi ishlatsa boladi
        userService.registerUser("john@example.com");
    }
}
```

Bu yerda muammo: agar `Database` klassining konstruktori ozgarsa, hamma joyda ozgartirish kerak. Agar `UserService` ga yangi xususiyat qoshilsa, yana hamma joyda ozgartirish kerak.

## Spring bilan hayot

Spring bilan:

```java
// Spring bilan - Spring hamma narsani qiladi
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        // Spring Database, UserService, EmailService ni
        // oz-ozidan yaratadi va ulaydi
    }
}
```

Spring siz uchun:
1. Obektlarni yaratadi
2. Ularni bir-biriga ulaydi
3. Ularning hayot davrini boshqaradi
4. Kerak bolmaganda ularni yogotadi

## Springning asosiy qismlari

Spring bir necha qismlardan iborat:

- **Spring Core** - eng asosiy qism, obektlarni yaratadi
- **Spring Boot** - Springni oson ishga tushirish uchun
- **Spring MVC** - web dasturlar yaratish uchun
- **Spring Data** - malumotlar bazasi bilan ishlash uchun
- **Spring Security** - xavfsizlik uchun

## Xulosa

Spring - bu sizning Java dasturingizni boshqaruvchi "usta". U sizni mayda-chuyda tashvishlardan qutqaradi, siz esa faqat biznes logikaga (dasturingiz asosiy vazifasiga) diqqat qilasiz.

Springga "obektlarni boshqaruvchi mashina" desak ham boladi.
