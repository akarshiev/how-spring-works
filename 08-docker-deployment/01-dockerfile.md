# Dockerfile - Spring Boot app uchun

## Docker nima?

Docker = ilovangizni konteyner ichiga joylab, istalgan joyda ishlatish.

Tasavvur qiling: siz ilovani yozdingiz. Bu ilova ishlashi uchun Java, PostgreSQL, va boshqa narsalar kerak.

Docker bilan:

```bash
# 1 marta build
docker build -t my-app .

# Istalgan serverda ishga tushirish
docker run -p 8080:8080 my-app
```

## Dockerfile

```dockerfile
# 1-bosqich: Build (Maven)
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# pom.xml ni kopiyalash va dependency larni yuklab olish
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Manba kodini kopiyalash
COPY src ./src

# Ilovani build qilish
RUN mvn package -DskipTests -B

# 2-bosqich: Runtime (faqat Java)
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Build bosqichidan jar faylni olish
COPY --from=build /app/target/*.jar app.jar

# 8080 portni ochish
EXPOSE 8080

# Ilovani ishga tushirish
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Multi-stage build

Dockerfile yuqorida 2 bosqichdan iborat:

1. **Build stage** - Maven bilan ilovani build qilish
2. **Runtime stage** - faqat Java va jar fayl

Bu nima uchun kerak? Build stage da Maven, JDK va boshqa narsalar bor (katta hajm). Runtime stage da faqat JRE va jar fayl bor (kichik hajm).

```
Build stage: 500 MB (Maven + JDK + kod)
Runtime stage: 100 MB (faqat JRE + jar)
```

## .dockerignore

```dockerignore
# .dockerignore - Docker build ga keraksiz fayllarni yubormaslik
target/
.git/
.idea/
*.md
.gitignore
```

## Docker image yaratish va ishga tushirish

```bash
# Image yaratish
docker build -t my-spring-app:1.0 .

# Container ni ishga tushirish
docker run -p 8080:8080 --name my-app my-spring-app:1.0

# Backgroundda ishga tushirish
docker run -d -p 8080:8080 --name my-app my-spring-app:1.0

# Loglarni korish
docker logs -f my-app

# Container ni to'xtatish
docker stop my-app

# Container ni ochirish
docker rm my-app
```

## Xulosa

- Dockerfile -> ilovani konteynerga joylash qoidalari
- Multi-stage build -> kichik hajmli image
- Build stage -> Maven bilan build
- Runtime stage -> faqat JRE + jar
- docker build -> image yaratish
- docker run -> container ni ishga tushirish
