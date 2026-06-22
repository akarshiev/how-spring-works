# Embedded Server — Tomcat ichki qanday ishlaydi?

Spring Boot ilovasini ishga tushirsangiz, alohida server o'rnatmasdan ham web ilova ishlaydi. Bu embedded (ichki) server mexanizmi tufayli.

## Odatiy vs Embedded server

**Odatiy yondashuv (Spring Boot'siz):**
1. Tomcat'ni alohida yuklab, o'rnatish
2. Ilovani `.war` sifatida build qilish
3. `.war` faylni Tomcat'ning `webapps/` papkasiga joylash
4. Tomcat'ni qayta ishga tushirish

**Embedded yondashuv (Spring Boot bilan):**
```bash
java -jar my-app.jar  # Server ichkarida, hamma narsa tayyor
```

## Qanday ishlaydi?

`spring-boot-starter-web` qo'shilganda, Tomcat ham dependency sifatida keladi:

```xml
spring-boot-starter-web
  +-- spring-boot-starter-tomcat  <- Tomcat bu yerda
        +-- tomcat-embed-core
        +-- tomcat-embed-el
        +-- tomcat-embed-websocket
```

Spring Boot ishga tushganda `TomcatServletWebServerFactory` bean'i Tomcat'ni yaratadi va konfiguratsiya qiladi:

```
SpringApplication.run() chaqiriladi
         |
         v
ApplicationContext yaratiladi
         |
         v
TomcatAutoConfiguration ishlaydi
         |
         v
Tomcat server yarailadi va 8080 portda ishga tushadi
         |
         v
DispatcherServlet Tomcat'ga ro'yxatdan o'tadi
         |
         v
Ilova HTTP so'rovlarni qabul qilishga tayyor
```

## Portni o'zgartirish

```properties
# application.properties
server.port=9090
server.servlet.context-path=/api

# Random port (test uchun foydali)
server.port=0
```

```java
// Yoki konfiguratsiya orqali
@Component
public class ServerConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.setPort(9090);
        factory.setContextPath("/api");
        // Thread pool sozlamalari
        factory.addConnectorCustomizers(connector -> {
            connector.setAttribute("maxThreads", 200);
        });
    }
}
```

## Tomcat'ni Undertow bilan almashtirish

Undertow yuqori yuklamali ilovalar uchun yaxshiroq performance ko'rsatadi:

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

Kod o'zgartirishsiz — faqat dependency o'zgartirish yetarli.

## HTTPS sozlash

```properties
server.port=8443
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=my-app
```

```java
// HTTP'ni HTTPS'ga yo'naltirish
@Configuration
public class HttpsConfig {

    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint constraint = new SecurityConstraint();
                constraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                constraint.addCollection(collection);
                context.addConstraint(constraint);
            }
        };

        factory.addAdditionalTomcatConnectors(httpConnector());
        return factory;
    }

    private Connector httpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }
}
```

## So'rov qanday yo'l bosadi?

```
HTTP so'rov keladi (GET /api/users/1)
         |
         v
Tomcat (Connector) so'rovni qabul qiladi
         |
         v
Spring Security filterlari ishlaydi
         |
         v
DispatcherServlet so'rovni qabul qiladi
         |
         v
Handler Mapping kerakli Controller'ni topadi
         |
         v
UserController.getUser(1) chaqiriladi
         |
         v
Javob JSON sifatida qaytadi
         |
         v
Tomcat javobni client'ga yuboradi
```
