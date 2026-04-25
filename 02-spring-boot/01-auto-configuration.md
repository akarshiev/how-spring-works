# @SpringBootApplication ichida nima bor?

## @SpringBootApplication qanday ishlaydi?

Bu annotation - bu bitta emas, 3 ta annotationning birikmasi:

```java
@SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
```

Keling, har birini korib chiqamiz.

## 1. @Configuration

Bu Springga "bu klassda beanlar bor" deb aytadi.

```java
@Configuration  // Spring: "Bu yerda konfiguratsiya bor"
public class AppConfig {
    
    @Bean  // Spring: "UserService bean yaratish kerak"
    public UserService userService() {
        return new UserService();
    }
}
```

## 2. @ComponentScan

Springga qayerdan klasslarni qidirish kerakligini aytadi.

```java
@SpringBootApplication
// @ComponentScan("com.example") - bu avtomatik qoshiladi
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Spring qidirishni sizning main klassingiz joylashgan papkadan boshlaydi:

```
com/example/
  +-- Application.java (mana shu yerda)
  +-- controller/
  +-- service/
  +-- repository/
```

@ComponentScan shu papka va uning ichidagi hamma papkalardan @Component, @Service, @Repository, @Controller larni topadi.

## 3. @EnableAutoConfiguration

Spring Bootning eng kuchli qismi. Spring qoshib qoyilgan maven dependency larni koradi va shunga qarab sozlamalarni avtomatik qiladi.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Spring:

```
"spring-boot-starter-web bor -> Web ilova -> Tomcat ni ishga tushir -> DispatcherServlet ni sozla"
```

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

Spring:

```
"JPA bor -> DataSource ni sozla -> EntityManagerFactory ni yarat -> Hibernate ni yukla"
```

## @EnableAutoConfiguration qanday ishlaydi?

Spring Bootning ichida `spring.factories` degan fayl bor. Bu faylda qanday klasslarni avtomatik ishga tushirish kerakligi yozilgan.

```
# META-INF/spring.factories faylining ichida:
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration,\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
  ... 100 dan ortiq auto-configuration klasslari
```

Spring Boot shu klasslarni tekshiradi. Agar:
- Kerakli dependency bor bolsa -> klassni ishga tushiradi
- Kerakli dependency yoq bolsa -> klassni otkazib yuboradi

## Avtomatik konfiguratsiya qanday ishlaydi?

```java
@Configuration
@ConditionalOnClass(DataSource.class)  // Faqat DataSource classi bolsa
@ConditionalOnMissingBean(DataSource.class)  // Va DataSource bean yaratilmagan bolsa
@EnableConfigurationProperties(DataSourceProperties.class)  // application.properties dan ol
public class DataSourceAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean  // Agar foydalanuvchi oz DataSource bermagan bolsa
    public DataSource dataSource(DataSourceProperties properties) {
        // application.properties dan url, username, password ni olib
        // DataSource yaratadi
        return DataSourceBuilder.create()
            .url(properties.getUrl())
            .username(properties.getUsername())
            .password(properties.getPassword())
            .build();
    }
}
```

## @ConditionalOn... annotatsiyalari

| Annotation | Nima tekshiradi? |
|-----------|-----------------|
| @ConditionalOnClass | Klass classpathda bormi? |
| @ConditionalOnMissingBean | Bean hali yaratilmaganmi? |
| @ConditionalOnProperty | application.properties da property bormi? |
| @ConditionalOnExpression | SpEL ifoda bajariladimi? |

## Xulosa

@SpringBootApplication = 3 ta annotationning birikmasi:

```
@SpringBootApplication
    |
    +-- @Configuration       -> "Bu yerda bean config bor"
    +-- @ComponentScan        -> "Klasslarni mana bu papkadan qidir"
    +-- @EnableAutoConfiguration -> "Dependencylarga qarab sozlamalarni avtomatik qil"
```
