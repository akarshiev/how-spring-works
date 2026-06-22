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

// ===== BOGLIQLIKLAR =====
// spring-boot-starter-web -> REST API
// spring-boot-starter-actuator -> auto-configuration larni korish uchun
// spring-boot-starter-data-jpa -> @ConditionalOnClass namoyish qilish uchun (ixtiyoriy)
// h2 -> JPA bilan ishlasa kerak boladi
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Bu dependency izohda: @ConditionalOnClass(DataSource.class) qanday ishlashini korsatadi
    // Izohni ochsangiz, DataSourceAutoConfiguration avtomatik ishga tushadi
    // implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // runtimeOnly("com.h2database:h2")
}
