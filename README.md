# How Spring Works

Spring Boot'ni o'zbek tilida, vizual va amaliy tarzda o'rganing.

[howhttps.works](https://howhttps.works) loyihasidan ilhomlanib yaratilgan — lekin bu yerda Spring Framework'ning ichki mexanizmlari tushuntiriladi.

---

## Nima uchun bu repo?

O'zbek tilidagi Spring Boot resurslari juda kam. Bu repo har bir mavzuni **"qanday ishlaydi"** savoli orqali tushuntiradi — nafaqat qanday yozishni, balki nima uchun shunday ishlashini ham.

Har bir bo'lim:
- Mavzuning asosiy tushunchasini real hayot misoli bilan tushuntiradi
- Ishlaydigan kod namunalari beradi
- Spring Boot'da qanday qo'llanilishini ko'rsatadi

---

## Bo'limlar

| # | Bo'lim | Mavzular |
|---|--------|----------|
| 01 | [Spring Core](./01-spring-core/) | IoC, DI, Bean, ApplicationContext |
| 02 | [Spring Boot](./02-spring-boot/) | Auto-configuration, Starter, Properties, Profiles |
| 03 | [REST API](./03-rest-api/) | HTTP, Controller, DTO, Exception Handling, Validation |
| 04 | [Data JPA](./04-data-jpa/) | JPA, Entity, Repository, JPQL, Transactions |
| 05 | [Spring Security](./05-spring-security/) | Authentication, JWT, UserDetails, Method Security |
| 06 | [Database](./06-database/) | PostgreSQL, SQL, Flyway, HikariCP |
| 07 | [Testing](./07-testing/) | JUnit 5, Mockito, Integration Testing |
| 08 | [Docker](./08-docker-deployment/) | Dockerfile, Docker Compose, Environment Variables |

---

## Qanday o'qish kerak?

Bo'limlarni ketma-ket o'qing. Har bir mavzu oldingisiga asoslanadi. Kodni faqat o'qimang — ishlaydigan misollarni [`examples/`](./examples/) papkasida topib, o'zingiz ishga tushiring.

---

## Misollar

Har bir bo'lim uchun ishlaydigan Spring Boot proyekti `examples/` papkasida joylashgan:

```
examples/
├── 01-hello-spring/        Spring Boot minimal app
├── 03-rest-api-demo/       CRUD REST API
├── 04-jpa-demo/            Entity + Repository + Service
└── 05-jwt-auth-demo/       JWT authentication
```

---

## Bog'lanish

- Telegram: [t.me/abdukarim_qarshiyev](https://t.me/abdukarim_qarshiyev)
- Blog: [akarshiev.blog](https://uz.akarshiev.blog)
- GitHub: [github.com/akarshiev](https://github.com/akarshiev)
