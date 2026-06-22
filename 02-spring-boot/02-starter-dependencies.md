# Starter Dependencies — Starterlar qanday ishlaydi?

Starter — bir nechta bog'liq kutubxonalarni bitta dependency sifatida taqdim etuvchi mexanizm.

## Muammo: qo'lda boshqarish

Spring Boot'siz web ilova uchun qo'lda ko'p dependency qo'shish kerak edi:

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>6.1.0</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>6.1.0</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.0</version>
</dependency>
<!-- ... va yana bir nechta ... -->
```

Versiyalar bir-biriga mos kelishi ham siz zimmasida.

## Yechim: Starter

```xml
<!-- 1 ta starter = ko'p kutubxona, moslashtiriladn versiyalar -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

`spring-boot-starter-web` ichida nima bor?

```
spring-boot-starter-web
  +-- spring-boot-starter          (Spring Boot asosi)
  +-- spring-boot-starter-tomcat   (Embedded Tomcat)
  +-- spring-webmvc                (Spring MVC)
  +-- jackson-databind             (JSON)
  +-- jackson-datatype-jdk8        (Java 8 tiplari)
  +-- jackson-module-parameter-names
```

Bitta qator bilan 7+ kutubxona keladi, versiyalari mos.

## Eng ko'p ishlatiladigan Starterlar

| Starter | Nima qiladi? |
|---------|-------------|
| `spring-boot-starter-web` | REST API va web ilovalar |
| `spring-boot-starter-data-jpa` | JPA + Hibernate + DataSource |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ |
| `spring-boot-starter-validation` | Bean Validation |
| `spring-boot-starter-mail` | Email yuborish |
| `spring-boot-starter-actuator` | Monitoring va health check |
| `spring-boot-starter-thymeleaf` | HTML shablonlar |
| `spring-boot-starter-data-redis` | Redis bilan ishlash |

## Starter + Auto-Configuration birgalikda

Starter kutubxonani classpath'ga qo'shadi. Auto-Configuration esa shu kutubxona borligini ko'rib, sozlamalarni avtomatik qiladi:

`spring-boot-starter-data-jpa` qo'shildi → Classpath'da `DataSource`, `EntityManager` classlari paydo bo'ldi → `DataSourceAutoConfiguration` va `HibernateJpaAutoConfiguration` ishga tushdi → Siz faqat `application.properties`'da DB URL'ni ko'rsatasiz.

## Spring Boot Parent BOM

`spring-boot-starter-parent` barcha starter versiyalarini boshqaradi:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<!-- Versiya ko'rsatish shart emas — parent hal qiladi -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

## Starter ichidagi versiyani o'zgartirish

Ba'zan starter bilan kelgan versiya siz istaganidan farq qiladi:

```xml
<properties>
    <!-- Starter bilan kelgan versiyadан farqli PostgreSQL driver -->
    <postgresql.version>42.6.0</postgresql.version>
</properties>
```

Yoki dependency exclusion bilan:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <!-- Tomcat o'rniga Undertow ishlatish uchun -->
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-undertow</artifactId>
</dependency>
```
