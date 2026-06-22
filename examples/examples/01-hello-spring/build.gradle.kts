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
// spring-boot-starter-web -> Tomcat + Spring MVC + Jackson
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}
