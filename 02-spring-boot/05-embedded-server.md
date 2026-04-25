# Embedded Server - Tomcat ichki qanday ishlaydi?

## Embedded server nima?

Embedded = ichiga joylashtirilgan

Spring Boot ilovasining ICHIDA Tomcat server bor. Siz alohida server o'rnatishingiz shart emas.

## Odatiy vs Embedded

### Odatiy usul (Spring Bootsiz):

1. Tomcat ni alohida yuklab olasiz
2. Tomcat ni o'rnatasiz
3. Ilovangizni .war qilib build qilasiz
4. .war faylni Tomcat ichiga tashlaysiz
5. Tomcat ni ishga tushirasiz

### Embedded usul (Spring Boot bilan):

1. `java -jar my-app.jar` yozasiz
2. Tomcat ichkarida oz-ozidan ishga tushadi

## Qanday ishlaydi?

Spring Bootda Tomcat starter bor:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
</dependency>
```

Bu starter Tomcat ni ilova ichiga joylashtiradi. Siz hatto Tomcat borligini sezmaysiz.

## Tomcat ni Spring Boot qanday ishga tushiradi?

1. Spring Boot ishga tushadi
2. TomcatAutoConfiguration klassi ishlaydi
3. Tomcat server yaratiladi va sozlanadi
4. DispatcherServlet yaratiladi
5. Tomcat 8080 portda ishga tushadi

```java
// Spring Boot ichida taxminan shunday ishlaydi
@Configuration
public class EmbeddedTomcat {
    
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setPort(8080);  // Default port
        factory.setContextPath("/api");  // Context path
        
        // SSL sozlamalari
        factory.addConnectorCustomizers(connector -> {
            connector.setProperty("compression", "on");
        });
        
        return factory;
    }
}
```

## Portni ozgartirish

```properties
# application.properties
server.port=9090
server.servlet.context-path=/myapp
```

Yoki:

```java
@Component
public class CustomContainer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.setPort(9090);
        factory.setContextPath("/myapp");
    }
}
```

## Tomcat ni Undertow yoki Jetty ga almashtirish

Spring Boot Tomcat default. Agar boshqa server kerak bolsa:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-undertow</artifactId>
</dependency>
```

## HTTPS sozlash

```properties
server.port=443
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=secret
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```

## Server ichida nima boladi?

```
Spring Boot ilovasi
      |
      v
   Tomcat (ichki)
      |
      +-- HTTP so'rovni qabul qiladi
      +-- DispatcherServlet ga yuboradi
      +-- DispatcherServlet @Controller ga yuboradi
      +-- Javobni qaytaradi
```

## Xulosa

- Embedded server = ilova ichidagi server
- Spring Boot default Tomcat bilan keladi
- Alohida server o'rnatish shart emas
- `java -jar` bilan to'g'ridan-to'g'ri web ilova ishlaydi
- Agar kerak bo'lsa, Undertow yoki Jetty ga almashtirish mumkin
