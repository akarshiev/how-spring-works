# Dockerfile — Spring Boot app uchun

Docker — ilovangizni konteyner ichiga joylab, istalgan muhitda bir xil ishlatish imkonini beradi. "Menda ishlaydi lekin serverda ishlamaydi" muammosi yo'qoladi.

## Nima uchun Docker?

Spring Boot ilovasi ishga tushishi uchun Java kerak. Serverni topasiz, Java o'rnatasiz, versiyalar mos keladi deb umid qilasiz. Docker bilan bu muammo yo'q — Java va ilova birgalikda "qutida" bo'ladi:

```bash
docker build -t my-app .
docker run -p 8080:8080 my-app
# Istalgan serverda. Java o'rnatishsiz.
```

## Multi-stage Dockerfile

Spring Boot uchun eng yaxshi yondashuv — ikki bosqichli build:

```dockerfile
# =========================
# 1-BOSQICH: Build
# =========================
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Dependency'larni alohida yuklash (layer cache uchun)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Manba kodi
COPY src ./src

# Build (test o'tkazib yuborish)
RUN ./mvnw package -DskipTests -B

# =========================
# 2-BOSQICH: Runtime
# =========================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Xavfsizlik: root bo'lmagan foydalanuvchi
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Faqat jar faylni ko'chirish (Maven, JDK yo'q — kichik image)
COPY --from=build /app/target/*.jar app.jar

# Foydalanuvchini o'zgartirish
USER spring:spring

# Port
EXPOSE 8080

# Ilova ishga tushirish — JVM sozlamalari bilan
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
```

Nima uchun ikki bosqich? Build bosqichida Maven + JDK + ko'p narsalar bor — 500MB+. Runtime bosqichida faqat JRE + jar — 150-200MB.

## Layer caching

Dockerfile'da muhim tartib: kam o'zgaruvchi qatorlar yuqorida, ko'p o'zgaruvchi pastda:

```dockerfile
# YAXSHI — dependency'lar alohida qatlam (kamdan-kam o'zgaradi)
COPY pom.xml .
RUN ./mvnw dependency:go-offline  # Bu kesh — pom.xml o'zgarmasa qayta yuklanmaydi

COPY src ./src                    # Bu ko'p o'zgaradi
RUN ./mvnw package                # Har marta qayta build

# YOMON — hamma narsani birgalikda (kesh ishlamaydi)
COPY . .
RUN ./mvnw package
```

## .dockerignore

```dockerignore
# .dockerignore — Docker build kontekstiga yubormaslik
target/
.git/
.github/
.idea/
*.iml
.mvn/wrapper/maven-wrapper.jar
**/*.md
.gitignore
docker-compose*.yml
```

## Qurilish va ishga tushirish

```bash
# Image yaratish
docker build -t my-spring-app:1.0.0 .

# Ishga tushirish
docker run \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host:5432/mydb \
  -e DB_PASSWORD=secret \
  --name my-app \
  my-spring-app:1.0.0

# Background'da
docker run -d -p 8080:8080 --name my-app my-spring-app:1.0.0

# Loglarni ko'rish
docker logs -f my-app

# To'xtatish va o'chirish
docker stop my-app && docker rm my-app

# Image hajmini ko'rish
docker images my-spring-app
```

## JVM sozlamalari konteyner uchun

```dockerfile
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:InitialRAMPercentage=50.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

`-XX:+UseContainerSupport` — JVM konteynerning haqiqiy xotira limitini ko'radi (eski Java'da muammo bo'lardi). `-XX:MaxRAMPercentage=75.0` — konteyner xotirasining 75% ishlatilsin.

## Gradle bilan

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
COPY src ./src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

## Spring Boot Layered Jar (muqobil yondashuv)

Spring Boot 2.3+ da jar qatlamlarini ajratish:

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar

# Jar'ni qatlamlarga ajratish
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=runtime /app/dependencies/ ./
COPY --from=runtime /app/spring-boot-loader/ ./
COPY --from=runtime /app/snapshot-dependencies/ ./
COPY --from=runtime /app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

Bu yondashuv katta ilovalar uchun rebuild vaqtini qisqartiradi — faqat o'zgargan qatlam rebuild qilinadi.
