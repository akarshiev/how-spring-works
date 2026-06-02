plugins {
    java
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "com.example"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

// ===== BOG'LIQLIKLAR =====
// spring-boot-starter-web -> REST API
// spring-boot-starter-security -> login, JWT, xavfsizlik
// spring-boot-starter-data-jpa -> JPA (foydalanuvchilarni saqlash)
// h2 -> test uchun RAM database
// jjwt -> JWT token yaratish va tekshirish
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")

    // JWT (JSON Web Token) - versiya o'zgaruvchi orqali boshqariladi
    val jjwtVersion = "0.12.5"
    implementation("io.jsonwebtoken:jjwt-api:${jjwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jjwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jjwtVersion}")
}
