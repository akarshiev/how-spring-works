# Application Properties - Sozlamalar

## application.properties nima?

Bu Spring Boot ilovasining sozlamalar fayli. Bu yerda siz ilovangizni qanday ishlashini boshqarasiz.

## Nega kerak?

Ilovadagi sonlar, matnlar, URL larni kodga qattiq yozish (hardcode) yomon usul. Yaxshi usul - ularni application.properties ga yozish.

Yomon usul:

```java
@Service
public class EmailService {
    // Kod ichiga yozilgan - yomon!
    private String host = "smtp.gmail.com";
    private int port = 587;
    private String username = "admin@gmail.com";
    private String password = "secret123";
}
```

Yaxshi usul:

```java
@Service
public class EmailService {
    @Value("${email.host}")      // application.properties dan oladi
    private String host;
    
    @Value("${email.port}")
    private int port;
}
```

application.properties da:

```properties
email.host=smtp.gmail.com
email.port=587
email.username=admin@gmail.com
email.password=secret123
```

## .properties vs .yml (YAML)

Spring Boot ikkala formatni ham qollab-quvvatlaydi.

### .properties formati:

```properties
server.port=8080
server.servlet.context-path=/api

spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=123

app.name=My Application
app.description=Bu mening ilovam
```

### .yml (YAML) formati:

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: postgres
    password: "123"

app:
  name: My Application
  description: Bu mening ilovam
```

**Qaysi biri yaxshi?** - YAML osonroq oqiladi, lekin .properties soddaroq.

## @Value - Qiymatlarni olish

Eng oddiy usul:

```java
@Component
public class AppConfig {
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version:1.0.0}")  // :1.0.0 = default qiymat (agar topilmasa)
    private String version;
    
    @Value("${server.port}")
    private int port;
    
    @Value("${spring.datasource.url}")
    private String dbUrl;
}
```

## @ConfigurationProperties - Strukturali usul

Yaxshiroq usul - guruhlab olish:

```properties
app.name=My App
app.version=2.0.0
app.email.from=admin@example.com
app.email.to=support@example.com
```

```java
@Component
@ConfigurationProperties(prefix = "app")  // "app." bilan boshlangan hamma property
public class AppProperties {
    private String name;
    private String version;
    private Email email = new Email();  // ichki klass
    
    // getter va setter lar
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }
    
    // Ichki klass
    public static class Email {
        private String from;
        private String to;
        
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
    }
}
```

Ishlatish:

```java
@Service
public class EmailService {
    private final AppProperties properties;
    
    public EmailService(AppProperties properties) {
        this.properties = properties;
    }
    
    public void sendEmail() {
        System.out.println("From: " + properties.getEmail().getFrom());
        System.out.println("To: " + properties.getEmail().getTo());
    }
}
```

## Eng kop ishlatiladigan sozlamalar

```properties
# Server sozlamalari
server.port=8080
server.servlet.context-path=/api

# Malumotlar bazasi
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Log
logging.level.org.springframework=INFO
logging.level.com.example=DEBUG
logging.file.name=logs/app.log
```

## Environment properties - Muhim

```yaml
spring:
  application:
    name: my-app
  config:
    import: optional:file:.env[.properties]  # .env faylidan oqish
```

## 3 xil usul (qay tartibda ishlaydi)

Spring Boot sozlamalarni bir necha joydan qidiradi. Pastdagisi yuqoridagini yopadi:

1. application.properties / application.yml (asosiy)
2. application-{profile}.properties (masalan application-dev.properties)
3. Environment variables (OS da)
4. Command line argumentlari (`--server.port=9090`)

## Xulosa

- Sozlamalarni kodga qattiq yozmang -> application.properties ga yozing
- @Value -> bitta qiymat olish
- @ConfigurationProperties -> guruhlab olish (tavsiya etiladi)
- .properties yoki .yml - qaysi biri sizga yoqsa
