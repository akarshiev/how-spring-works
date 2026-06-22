# Environment Config — Sozlamalar va Maxfiy ma'lumotlar

Parol, API kalit, ma'lumotlar bazasi URL'i kabi maxfiy ma'lumotlarni kodga yozish — eng xavfli xato. GitHub'da ko'rinadi, o'zgartirganda kodni qayta build qilish kerak.

## Muammo: hardcode

```java
// YOMON — hech qachon bunday qilmang
@Service
public class EmailService {
    private final String apiKey = "sk-1234567890abcdef";  // GitHub'da ko'rinadi!
    private final String dbPassword = "SuperSecret123";    // Xavfli!
}
```

## Yechim: Environment variables

```properties
# application.properties — faqat havola, qiymat emas
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

jwt.secret=${JWT_SECRET}
email.api-key=${EMAIL_API_KEY}

# Default qiymat bilan
server.port=${PORT:8080}
spring.profiles.active=${PROFILE:dev}
```

```bash
# Production serverda — tashqaridan berish
export DB_URL=jdbc:postgresql://prod-db:5432/myapp
export DB_USER=prod_user
export DB_PASSWORD=VerySecretProdPassword
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

java -jar app.jar
```

## .env fayli — development uchun

```bash
# .env fayli — faqat local development
DB_URL=jdbc:postgresql://localhost:5432/dev_db
DB_USER=postgres
DB_PASSWORD=postgres
JWT_SECRET=dev_secret_key_not_for_production
PROFILE=dev
```

```properties
# application.properties
spring.config.import=optional:file:.env[.properties]
```

**Muhim:** `.env` faylini `.gitignore`'ga qo'shing:

```gitignore
# .gitignore
.env
.env.local
.env.production
```

Faqat `.env.example` fayli Git'da — faqat kalit nomlar, qiymatlar emas:

```bash
# .env.example — bu Git'da
DB_URL=
DB_USER=
DB_PASSWORD=
JWT_SECRET=
```

## Docker Compose bilan

```yaml
# docker-compose.yml
services:
  app:
    environment:
      # To'g'ridan-to'g'ri
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://postgres:5432/myapp_db
      # .env fayldan
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}

    # Yoki butun .env faylni yuklash
    env_file:
      - .env
```

```bash
# Docker run bilan
docker run \
  --env-file .env \
  -p 8080:8080 \
  my-spring-app:1.0.0
```

## Spring profiles bilan birgalikda

```
application.properties          <- umumiy
application-dev.properties      <- development sozlamalari
application-prod.properties     <- production sozlamalari (maxfiy qiymatlar ENV'dan)
```

```properties
# application-prod.properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.show-sql=false
logging.level.root=WARN
server.port=${PORT:8080}
```

```bash
# Production'da
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://...
DB_PASSWORD=...
java -jar app.jar
```

## @Value bilan olish

```java
@Component
public class AppConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${server.port:8080}")  // Default qiymat
    private int port;

    @Value("${feature.payments.enabled:false}")
    private boolean paymentsEnabled;
}
```

## @ConfigurationProperties — strukturali

```java
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Database database = new Database();
    private Jwt jwt = new Jwt();

    public static class Database {
        private String url;
        private String username;
        private String password;
        // getter/setter
    }

    public static class Jwt {
        private String secret;
        private long expirationMs = 86_400_000L;
        // getter/setter
    }
}
```

```properties
app.database.url=${DB_URL}
app.database.username=${DB_USER}
app.database.password=${DB_PASSWORD}
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=3600000
```

## Kubernetes Secrets

```yaml
# k8s-secret.yaml — Base64 encoded qiymatlar
apiVersion: v1
kind: Secret
metadata:
  name: myapp-secrets
type: Opaque
data:
  DB_PASSWORD: bXlQcm9kUGFzc3dvcmQ=   # base64("myProdPassword")
  JWT_SECRET: c2VjcmV0S2V5MTIz        # base64("secretKey123")
```

```yaml
# k8s-deployment.yaml
spec:
  containers:
  - name: app
    image: my-spring-app:1.0.0
    env:
    - name: DB_PASSWORD
      valueFrom:
        secretKeyRef:
          name: myapp-secrets
          key: DB_PASSWORD
    - name: JWT_SECRET
      valueFrom:
        secretKeyRef:
          name: myapp-secrets
          key: JWT_SECRET
```

## Xavfsizlik qoidalari

Parollarni tekshirish uchun: `git log --all --full-history -- .env` — agar qachondir commit qilingan bo'lsa, history'da qoladi. `git filter-branch` yoki BFG Repo-Cleaner bilan tozalash kerak.

Kuchli secret generatsiya qilish:

```bash
# JWT secret uchun (256-bit, Base64)
openssl rand -base64 32

# DB parol uchun
openssl rand -base64 16
```

Production'da environment variables — minimal talab. Katta loyihalarda HashiCorp Vault, AWS Secrets Manager, Azure Key Vault kabi maxsus tizimlardan foydalaniladi.
