# @SpringBootApplication ichida nima bor?

Spring Boot loyihasida hamma narsa bitta annotation bilan boshlanadi:

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Bu annotation aslida uchta annotation'ning birikmasidir:

```java
@SpringBootApplication
= @Configuration + @EnableAutoConfiguration + @ComponentScan
```

## @Configuration

Bu klassda Spring bean'lari aniqlanadi deb bildiradi:

```java
@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## @ComponentScan

Spring qaysi papkadan komponentlarni qidirishi kerakligini belgilaydi. Default — `main` klassi joylashgan papka va uning ichidagi barcha papkalar:

```
com.example/
  +-- Application.java    <- @ComponentScan shu yerdan boshlanadi
  +-- controller/
  |     +-- UserController.java      <- topiladi
  +-- service/
  |     +-- UserService.java         <- topiladi
  +-- repository/
        +-- UserRepository.java      <- topiladi
```

`com.example` dan tashqaridagi hech narsa avtomatik topilmaydi.

## @EnableAutoConfiguration

Spring Boot'ning eng kuchli qismi. Classpath'da qaysi kutubxona borligiga qarab, sozlamalarni avtomatik qiladi.

![Spring Boot Auto-Configuration](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/images/spring-boot-auto-configuration.png)

`spring-boot-starter-web` qo'shilsa — Spring Tomcat, DispatcherServlet, Jackson'ni avtomatik sozlaydi.

`spring-boot-starter-data-jpa` qo'shilsa — DataSource, EntityManagerFactory, Hibernate avtomatik sozlanadi.

## Avtomatik konfiguratsiya mexanizmi

Spring Boot `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` faylida yuzlab konfiguratsiya klassi ro'yxatini saqlaydi. Har biri muayyan shart bajarilgandagina ishga tushadi:

```java
// Spring Boot ichidagi DataSourceAutoConfiguration taxminan shunday:
@AutoConfiguration
@ConditionalOnClass(DataSource.class)         // DataSource classi bormi?
@ConditionalOnMissingBean(DataSource.class)   // Foydalanuvchi o'zi bermagan bo'lsa
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource(DataSourceProperties properties) {
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
| `@ConditionalOnClass` | Klass classpath'da bormi? |
| `@ConditionalOnMissingBean` | Bean hali yaratilmaganmi? |
| `@ConditionalOnProperty` | application.properties'da property bormi? |
| `@ConditionalOnWebApplication` | Bu web ilovami? |
| `@ConditionalOnExpression` | SpEL ifoda to'g'rimi? |

## Avtomatik konfiguratsiyani o'chirish

Ba'zan Spring'ning avtomatik konfiguratsiyasi siz istaganidan farq qilishi mumkin:

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {
    // DataSource ni Spring avtomatik sozlamasin — o'zim qilaman
}
```

## application.properties orqali ta'sir o'tkazish

Avtomatik konfiguratsiya sozlamalarini properties orqali o'zgartirish mumkin:

```properties
# Tomcat portini o'zgartirish (WebMvcAutoConfiguration ta'sirida)
server.port=9090

# DataSource ni o'zgartirish (DataSourceAutoConfiguration ta'sirida)
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
```

Spring Boot'ning hujjatlarida har bir auto-configuration qaysi properties'ni qabul qilishini topish mumkin.
