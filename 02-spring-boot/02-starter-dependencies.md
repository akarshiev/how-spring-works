# Starter Dependencies - Starterlar qanday ishlaydi?

## Starter nima?

Starter - bu bir nechta dependency larni birlashtirgan bitta dependency.

Oddiy qilib: **Starter = bir nechta library ni bir paketga yig'ib berish**.

## Starter nega kerak?

Springsiz web ilova yaratish uchun:

```xml
<!-- Spring Bootsiz - 10 ta dependency ni ozingiz qoshasiz -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
</dependency>
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<!-- ... va yana 6 ta library ... -->
```

Spring Boot bilan:

```xml
<!-- 1 ta starter = 10 ta library -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## Starter ichida nima bor?

spring-boot-starter-web ni ichiga qarasak:

```xml
<!-- spring-boot-starter-web ning pom.xml ichida -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>  <!-- asos -->
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId>  <!-- server -->
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>  <!-- web framework -->
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>  <!-- JSON -->
    </dependency>
    <!-- ... va yana 10 ga yaqin library -->
</dependencies>
```

1 ta starter qoshsangiz -> Spring avtomatik ravishda 10-15 ta library ni qoshib oladi.

## Eng ko'p ishlatiladigan starterlar

| Starter | Nima qiladi? |
|---------|------------|
| spring-boot-starter-web | REST API va web ilovalar |
| spring-boot-starter-data-jpa | Malumotlar bazasi bilan ishlash |
| spring-boot-starter-security | Xavfsizlik |
| spring-boot-starter-test | Test (JUnit, Mockito, ...) |
| spring-boot-starter-thymeleaf | Frontend (HTML shablonlar) |
| spring-boot-starter-mail | Email yuborish |
| spring-boot-starter-validation | Malumotlarni tekshirish |
| spring-boot-starter-actuator | Monitoring va diagnostika |

## Starter + Auto-Configuration

Starter va auto-configuration birga ishlaydi.

Misollar:

```xml
<!-- 1. Web ilova -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
Spring avtomatik: Tomcat, DispatcherServlet, Jackson, ...

```xml
<!-- 2. Malumotlar bazasi -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```
Spring avtomatik: DataSource, EntityManagerFactory, JPA, ...

```xml
<!-- 3. Xavfsizlik -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
Spring avtomatik: Login formasi, CSRF, ...

## Starter yaratish

Agar siz oz starteringizni yaratmoqchi bolsangiz:

```xml
<project>
    <artifactId>my-starter</artifactId>
    
    <dependencies>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>my-library</artifactId>
        </dependency>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>my-other-library</artifactId>
        </dependency>
    </dependencies>
</project>
```

## Xulosa

Starter = bir nechta library ni bir joyga yig'ish.

- Bitta starter qoshsangiz -> kop library lar oz-ozidan qoshiladi
- Starter + auto-configuration -> hamma narsa avtomatik ishlaydi
- "spring-boot-starter-*" nomi bilan boshlanadi
