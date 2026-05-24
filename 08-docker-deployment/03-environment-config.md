# Environment Config - Environment variables va Secrets

## Environment Variables nima?

Environment variables = operatsion tizimdagi ozgaruvchilar. Ilova ishlashi uchun kerakli malumotlarni saqlaydi.

Muammo: parol, API kalit kabi maxfiy malumotlarni kodga yozib bolmaydi.

Yomon usul:

```java
// Kod ichida parol - XAVFLI!
spring.datasource.password=12345
```

Yaxshi usul:

```properties
# application.properties
spring.datasource.password=${DB_PASSWORD}
```

```bash
# Terminalda parolni o'rnatish
export DB_PASSWORD=12345
java -jar app.jar
```

## Spring Boot da Environment Variables

```properties
# application.properties da ishlatish
server.port=${PORT:8080}                    # Default: 8080
spring.datasource.url=${DB_URL}             # Majburiy
spring.datasource.username=${DB_USER:postgres} # Default: postgres
spring.datasource.password=${DB_PASSWORD}   # Majburiy
spring.profiles.active=${PROFILE:dev}       # Default: dev
```

## .env fayli

Development da ishlatish uchun:

```bash
# .env fayli (.gitignore ga qoshilgan)
DB_URL=jdbc:postgresql://localhost:5432/mydb
DB_USER=postgres
DB_PASSWORD=12345
PROFILE=dev
```

```properties
# application.properties
spring.config.import=optional:file:.env[.properties]
```

**.env fayli .gitignore da bolishi kerak!**

## Docker da Environment Variables

```yaml
# docker-compose.yml
services:
  app:
    build: .
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/myapp
      DB_USER: myapp_user
      DB_PASSWORD: myapp_pass
      PROFILE: prod
    
    # Yoki .env faylidan olish
    env_file:
      - .env
```

```bash
# Docker run da berish
docker run -e DB_URL=jdbc:postgresql://localhost:5432/mydb \
           -e DB_USER=postgres \
           -e DB_PASSWORD=secret \
           -e PROFILE=prod \
           my-app
```

## Kubernetes da Secrets

```yaml
# k8s-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
type: Opaque
data:
  DB_USER: bXlhcHBfdXNlcg==          # base64: myapp_user
  DB_PASSWORD: bXlhcHBfcGFzcw==      # base64: myapp_pass
```

```yaml
# k8s-deployment.yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: app
        image: my-app:1.0
        env:
        - name: DB_USER
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: DB_USER
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: DB_PASSWORD
```

## Security best practices

```properties
# application.properties da DEFAULT qiymatlar bolishi mumkin
# LEKIN haqiqiy parollar kiritilmasligi kerak!

# Yaxshi:
spring.datasource.password=${DB_PASSWORD}  # Environment dan ol

# Yomon:
spring.datasource.password=12345  # Kod ichida parol
```

## @Value bilan environment variable

```java
@Component
public class AppConfig {
    
    @Value("${DB_URL}")
    private String dbUrl;
    
    @Value("${DB_USER:postgres}")  // Default: postgres
    private String dbUser;
    
    @Value("${PROFILE:dev}")
    private String profile;
}
```

## Xulosa

- Environment variables -> maxfiy malumotlarni kodga yozmaslik
- ${VAR_NAME:default} -> environment variable ni olish
- .env fayli -> development uchun (gitignore da)
- Docker -> environment orqali berish
- Kubernetes -> Secret orqali berish
- Hech qachon parolni kodga yozmang
