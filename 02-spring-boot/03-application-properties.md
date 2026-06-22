# Application Properties — Sozlamalar

Ilovadagi sozlamalarni (parol, port, URL) to'g'ridan-to'g'ri kodga yozish — eng keng tarqalgan xatolardan biri. `application.properties` yoki `application.yml` aynan shu muammoni yechadi.

## Nima uchun sozlamalarni koddan ajratish kerak?

```java
// Yomon — sozlama kod ichida
@Service
public class EmailService {
    private final String host = "smtp.gmail.com";  // hardcode
    private final String password = "secret123";   // xavfli!
}
```

Bu yondashuv bilan:
- Production va development bir xil konfiguratsiya ishlatadi
- Maxfiy ma'lumotlar GitHub'da ko'rinib turadi
- Sozlamani o'zgartirish uchun kodni qayta build qilish kerak

```java
// Yaxshi — sozlama properties fayldan
@Service
public class EmailService {
    @Value("${email.host}")
    private String host;

    @Value("${email.password}")
    private String password;
}
```

## .properties vs .yml

Ikkala format ham qo'llab-quvvatlanadi:

```properties
# application.properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
app.name=My Application
```

```yaml
# application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: postgres

app:
  name: My Application
```

YAML ierarxik tuzilmani ko'rsatishda yaxshiroq o'qiladi. `.properties` oddiy va to'g'ri. Ikkalasini bir loyihada aralashtirib ishlatmang — birini tanlang.

## @Value — bitta qiymatni olish

```java
@Component
public class AppConfig {

    @Value("${server.port}")
    private int port;

    @Value("${app.name:Nomsiz ilova}")  // :default — agar topilmasa
    private String appName;

    @Value("${feature.enabled:false}")  // Boolean ham ishlaydi
    private boolean featureEnabled;
}
```

## @ConfigurationProperties — guruhlab olish

Ko'p properties bo'lsa, ularni klass sifatida olish tuzilmani yaxshilaydi:

```properties
# application.properties
app.email.host=smtp.gmail.com
app.email.port=587
app.email.username=sender@gmail.com
app.email.password=${EMAIL_PASSWORD}
```

```java
@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {
    private String host;
    private int port;
    private String username;
    private String password;

    // getter va setter'lar (Lombok @Data ham bo'ladi)
}
```

```java
@Service
public class EmailService {
    private final EmailProperties emailProps;

    public EmailService(EmailProperties emailProps) {
        this.emailProps = emailProps;
    }

    public void send(String to, String body) {
        // emailProps.getHost(), emailProps.getPort() ...
    }
}
```

`@ConfigurationProperties` `@Value`'dan afzal — katta konfiguratsiyalarda toza va testlash oson.

## Eng ko'p ishlatiladigan sozlamalar

```properties
# Server
server.port=8080
server.servlet.context-path=/api

# Ma'lumotlar bazasi
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Logging
logging.level.root=INFO
logging.level.com.example=DEBUG
logging.file.name=logs/app.log

# Jackson (JSON serialization)
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.default-property-inclusion=non_null
```

## Sozlamalarni o'qish tartibi

Spring Boot sozlamalarni bir nechta manbadan qidiradi, keyingisi oldingisini qayta yozadi:

```
1. application.properties / application.yml
2. application-{profile}.properties
3. OS Environment variables (SPRING_DATASOURCE_URL)
4. Command line argumentlari (--server.port=9090)
```

Bu tartib muhim: production'da Environment variable orqali parolni berish mumkin, `application.properties`'dagi eski qiymat o'chiriladi.

## Maxfiy ma'lumotlar uchun Environment variables

```properties
# application.properties — faqat havola, qiymat emas
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

```bash
# Production serverda
export DB_PASSWORD=realProductionPassword
export JWT_SECRET=256BitRandomSecretKey
java -jar app.jar
```

`.env` fayli development uchun qulay, lekin uni **hech qachon** Git'ga push qilmang:

```
# .gitignore
.env
```
