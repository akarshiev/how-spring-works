# Profiles - Turli muhitlar uchun sozlamalar

## Profile nima?

Profile - bu ilovaning turli muhitlarda (development, production, test) turlicha ishlashini taminlash.

## Real hayot misoli

Uyda va ishda turlicha kiyinasiz:

- **Uyda (dev)** -> shim, futbolka, uy shippak
- **Ishda (prod)** -> kostyum, galstuk, tufli

Ilova ham shunday:

- **Development** -> localhost database, debug log, hamma narsa ochiq
- **Production** -> real database, minimal log, xavfsizlik kuchli

## Profile yaratish

application-{profile}.properties nomli fayllar yaratamiz:

### Asosiy fayl (application.properties):

```properties
server.port=8080
spring.application.name=my-app
```

### Development profili (application-dev.properties):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/dev_db
spring.datasource.username=postgres
spring.datasource.password=123
spring.jpa.show-sql=true
logging.level.com.example=DEBUG
```

### Production profili (application-prod.properties):

```properties
spring.datasource.url=jdbc:postgresql://production-db:5432/prod_db
spring.datasource.username=prod_user
spring.datasource.password=${DB_PASSWORD}  # environment variable
spring.jpa.show-sql=false
logging.level.com.example=WARN
server.port=80
```

## Profilni qanday ishga tushirish?

### 1-usul: application.properties da

```properties
spring.profiles.active=dev
```

### 2-usul: Terminalda

```bash
java -jar my-app.jar --spring.profiles.active=prod
```

### 3-usul: Environment variable

```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar my-app.jar
```

### 4-usul: IDE da

IntelliJ IDEA -> Run Configuration -> VM Options:
```
-Dspring.profiles.active=dev
```

## @Profile - Kodni profilga bog'lash

```java
@Service
@Profile("dev")  // Faqat development rejimida ishlaydi
public class DevEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String message) {
        System.out.println("Dev: Email yuborildi -> " + to + " : " + message);
        // Aslida email yuborilmaydi, faqat konsolga chiqariladi
    }
}
```

```java
@Service
@Profile("prod")  // Faqat production rejimida ishlaydi
public class ProdEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String message) {
        // Haqiqiy email yuborish
        JavaMailSender.send(to, message);
    }
}
```

## Profillarni birlashtirish

```properties
# application.properties
spring.profiles.active=dev,postgres
```

Bir nechta profil bir vaqtda ishlashi mumkin:

```java
@Component
@Profile("dev & postgres")  // Ikkala profil ham aktiv bolsa
public class DevPostgresService {
}
```

## Default profile

Agar hech qanday profil aktiv bolmasa, "default" profili ishlaydi.

```java
@Component
@Profile("default")  // Faqat hech qanday profil tanlanmagan bolsa
public class DefaultService {
}
```

## Kod orqali profilni tekshirish

```java
@Component
public class ProfileChecker {
    @Autowired
    private Environment env;
    
    @PostConstruct
    public void check() {
        String[] profiles = env.getActiveProfiles();
        System.out.println("Aktiv profillar: " + Arrays.toString(profiles));
        
        if (env.acceptsProfiles("dev")) {
            System.out.println("Development rejimi");
        }
        
        if (env.acceptsProfiles("prod")) {
            System.out.println("Production rejimi");
        }
    }
}
```

## Qachon profile ishlatish kerak?

```yaml
# application.yml - bitta faylda hamma profillar
spring:
  application:
    name: my-app

---
# development
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password:

---
# production
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:postgresql://prod-db:5432/proddb
    username: ${DB_USER}
    password: ${DB_PASS}
```

## Xulosa

- Profile = ilovaning turli muhitlardagi holati
- Fayl nomi: `application-{profile}.properties`
- Aktivlashtirish: `spring.profiles.active=dev`
- Kodda @Profile bilan ajratish mumkin
- Eng kop ishlatiladigan: dev, test, prod
