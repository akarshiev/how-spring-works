package com.example.springbootdemo.controller;

import com.example.springbootdemo.config.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// @RestController = REST API controller
// Bu controller Spring Boot auto-configuration ni turli usullarda namoyish qiladi
@RestController
@RequestMapping("/api")
public class AutoConfigController {

    // ============ 1-usul: @Value orqali property larni olish ============
    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${server.port}")
    private int serverPort;

    // ============ 2-usul: Environment orqali property larni olish ============
    private final Environment environment;

    // ============ 3-usul: @ConfigurationProperties orqali (eng yaxshi usul) ============
    private final AppProperties appProperties;

    public AutoConfigController(Environment environment, AppProperties appProperties) {
        this.environment = environment;
        this.appProperties = appProperties;
    }

    // GET /api/info -> ilova haqida umumiy malumot
    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
            "appName", appName,                    // @Value dan
            "appVersion", appVersion,               // @Value dan
            "serverPort", serverPort,                // @Value dan
            "description", appProperties.getDescription(),  // @ConfigurationProperties dan
            "javaVersion", Runtime.version().toString(),    // Java versiyasi
            "activeProfile",                           // Environment dan
                String.join(", ", environment.getActiveProfiles())
        );
    }

    // GET /api/config-properties -> @ConfigurationProperties namoyishi
    @GetMapping("/config-properties")
    public Map<String, Object> configProperties() {
        return Map.of(
            "method", "@ConfigurationProperties(prefix = \"app\")",
            "howItWorks", "Spring Boot application.properties dan 'app.' bilan " +
                          "boshlangan hamma property larni avtomatik AppProperties ga yuklaydi",
            "values", Map.of(
                "app.name", appProperties.getName(),
                "app.version", appProperties.getVersion(),
                "app.features.greeting-enabled", appProperties.getFeatures().isGreetingEnabled(),
                "app.features.details-enabled", appProperties.getFeatures().isDetailsEnabled()
            )
        );
    }

    // GET /api/conditional-beans -> qaysi conditional bean lar yaratilganini korsatadi
    @GetMapping("/conditional-beans")
    public Map<String, Object> conditionalBeans() {
        // Bu yerda conditional bean larning holatini korsatamiz
        return Map.of(
            "greetingService", appProperties.getFeatures().isGreetingEnabled()
                ? "YARATILGAN (app.features.greeting-enabled=true)"
                : "YARATILMAGAN (app.features.greeting-enabled=false)",
            "detailsService", appProperties.getFeatures().isDetailsEnabled()
                ? "YARATILGAN (app.features.details-enabled=true)"
                : "YARATILMAGAN (app.features.details-enabled=false)",
            "note", "@ConditionalOnProperty qanday ishlaydi: " +
                    "application.properties da property bolsa -> bean yaratiladi, " +
                    "bolmasa -> yaratilmaydi"
        );
    }

    // GET /api/environment -> Environment dan barcha muhim property lar
    @GetMapping("/environment")
    public Map<String, Object> environment() {
        return Map.of(
            "spring.application.name",
                environment.getProperty("spring.application.name"),
            "server.port",
                environment.getProperty("server.port"),
            "java.runtime.name",
                environment.getProperty("java.runtime.name"),
            "os.name",
                environment.getProperty("os.name")
        );
    }

    // GET /api/auto-config-explanation -> auto-configuration tushuntirish
    @GetMapping("/auto-config-explanation")
    public Map<String, Object> autoConfigExplanation() {
        return Map.of(
            "whatIsAutoConfiguration",
                "@EnableAutoConfiguration -> Spring Boot classpath dagi jar fayllarga qarab," +
                " avtomatik ravishda bean lar yaratadi",
            "howItWorks", List.of(
                "1. spring-boot-starter-web qoshilsa -> Tomcat, DispatcherServlet, Jackson avtomatik",
                "2. spring-boot-starter-data-jpa qoshilsa -> DataSource, EntityManager, Hibernate avtomatik",
                "3. @ConditionalOnClass -> kerakli klass bormi? (masalan: DataSource.class)",
                "4. @ConditionalOnMissingBean -> foydalanuvchi oz beanini bermaganmi?",
                "5. @ConditionalOnProperty -> kerakli property application.properties da bormi?"
            ),
            "toSeeFullList",
                "GET http://localhost:8080/actuator/conditions -> " +
                "qaysi auto-configuration ishlagani va ishlamagani"
        );
    }
}
