# Spring Boot

Spring Boot - Spring Frameworkni oson ishlatish uchun qilingan qoshimcha.

## Mavzular

1. **01-auto-configuration.md** -> @SpringBootApplication ichida nima bor?
2. **02-starter-dependencies.md** -> Starterlar qanday ishlaydi?
3. **03-application-properties.md** -> application.properties vs yaml
4. **04-profiles.md** -> profile lar
5. **05-embedded-server.md** -> Tomcat ichki qanday ishlaydi?

## Nima farqi bor?

Oddiy Springda:

```java
// XML fayllar, config fayllar, maven qoshimchalari... ko'p ish
```

Spring Bootda:

```java
// 1 satr kod bilan hamma narsa tayyor
@SpringBootApplication
public class App { public static void main(String[] args) { SpringApplication.run(App.class, args); }}
```

Spring Boot = Spring + avtomatik sozlash + server ichida
