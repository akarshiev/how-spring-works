# Profiles — Turli muhitlar uchun sozlamalar

Ilova development, test va production muhitlarida har xil sozlamalar bilan ishlashi kerak. Development'da debug log va local DB, production'da minimal log va real DB.

Profile — aynan shu farqni boshqarish mexanizmi.

## Profile fayllari

`application-{profil_nomi}.properties` nomli fayllar yaratiladi:

```
src/main/resources/
  application.properties           <- umumiy, har doim o'qiladi
  application-dev.properties       <- development uchun
  application-prod.properties      <- production uchun
  application-test.properties      <- testlar uchun
```

```properties
# application.properties (umumiy)
spring.application.name=my-app
server.port=8080
```

```properties
# application-dev.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/dev_db
spring.datasource.username=postgres
spring.datasource.password=123
spring.jpa.show-sql=true
logging.level.com.example=DEBUG
```

```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://prod-server:5432/prod_db
spring.datasource.username=prod_user
spring.datasource.password=${DB_PASSWORD}
spring.jpa.show-sql=false
logging.level.com.example=WARN
```

## Profilni faollashtirish

To'rt xil usul:

```properties
# 1. application.properties orqali (development uchun qulay)
spring.profiles.active=dev
```

```bash
# 2. Terminalda (production uchun)
java -jar app.jar --spring.profiles.active=prod

# 3. Environment variable (Docker, CI/CD uchun)
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

```
# 4. IDE'da (IntelliJ IDEA Run Configuration)
VM options: -Dspring.profiles.active=dev
```

## @Profile — kodni profilga bog'lash

Bir interfeys, ikki implementatsiya: development'da soxta, production'da haqiqiy:

```java
public interface EmailService {
    void send(String to, String subject, String body);
}

@Service
@Profile("dev")  // Faqat development'da aktiv
public class ConsoleEmailService implements EmailService {
    @Override
    public void send(String to, String subject, String body) {
        System.out.println("Dev: Email -> " + to + ": " + subject);
        // Haqiqiy email yuborilmaydi
    }
}

@Service
@Profile("prod")  // Faqat production'da aktiv
public class SmtpEmailService implements EmailService {
    @Override
    public void send(String to, String subject, String body) {
        // Haqiqiy SMTP orqali yuborish
        javaMailSender.send(...);
    }
}
```

Spring aktiv profilga qarab qaysi implementatsiyani inject qilishni tanlaydi.

## YAML'da barcha profil bitta faylda

`---` ajratgichi bilan barcha profillar bitta `application.yml`'da yozilishi mumkin:

```yaml
spring:
  application:
    name: my-app

---
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/dev_db
    username: postgres
    password: "123"
  jpa:
    show-sql: true

---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:postgresql://prod-server:5432/prod_db
    username: prod_user
    password: ${DB_PASSWORD}
```

## Profil'ni kod ichida tekshirish

```java
@Component
public class ProfileChecker {

    @Autowired
    private Environment env;

    @PostConstruct
    public void check() {
        String[] activeProfiles = env.getActiveProfiles();
        System.out.println("Aktiv profillar: " + Arrays.toString(activeProfiles));

        if (env.acceptsProfiles(Profiles.of("prod"))) {
            System.out.println("Production rejimi");
        }
    }
}
```

## Qoidalar

`spring.profiles.active=dev`'ni `application.properties`'ga yozing, lekin bu qiymatni Git'ga push qiling. Production'da bu qiymat environment variable orqali qayta yoziladi.

Profillar soni kamroq bo'lishi yaxshiroq: `dev`, `test`, `prod` — ko'pchilik loyiha uchun yetarli. `staging`, `qa`, `uat` kabi ko'p profil boshqarishni qiyinlashtiradi.
