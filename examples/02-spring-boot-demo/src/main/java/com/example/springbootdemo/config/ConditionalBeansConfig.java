package com.example.springbootdemo.config;

// Bu klass @ConditionalOnProperty qanday ishlashini korsatadi.
// Bu Spring Boot auto-configuration ning asosiy mexanizmlaridan biri.
//
// @ConditionalOnProperty = "application.properties da bu property bolsa, shundagina bean yarat"
// @ConditionalOnMissingBean = "bu bean hali yaratilmagan bolsa, shundagina yarat"
// @ConditionalOnClass = "classpath da bu klass bolsa, shundagina yarat"
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConditionalBeansConfig {

    // ============ @ConditionalOnProperty namoyishi ============

    // app.features.greeting-enabled=true bolsa -> greetingService bean yaratiladi
    // app.features.greeting-enabled=false bolsa -> greetingService bean yaratilmaydi
    // havingValue = "true" -> faqat qiymat "true" ga teng bolsa
    // matchIfMissing = true -> agar property umuman yozilmagan bolsa, true deb hisobla
    @Bean
    @ConditionalOnProperty(
        prefix = "app.features",
        name = "greeting-enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public String greetingService() {
        System.out.println(">>> [AutoConfig] greetingService BEAN yaratildi (greeting-enabled=true)");
        return "greetingService is ready!";
    }

    // app.features.details-enabled=true bolsa -> detailsService bean yaratiladi
    @Bean
    @ConditionalOnProperty(
        prefix = "app.features",
        name = "details-enabled",
        havingValue = "true"
    )
    public String detailsService() {
        System.out.println(">>> [AutoConfig] detailsService BEAN yaratildi (details-enabled=true)");
        return "detailsService is ready!";
    }

    // ============ @ConditionalOnMissingBean namoyishi ============

    // Agar siz ozingiz DefaultWelcomeBean yaratmagan bolsangiz, Spring yaratadi
    // Bu "auto-configuration" ning asosiy prinsipi:
    //   "Agar foydalanuvchi oz variantini bermagan bolsa, men default ni beraman"
    @Bean
    @ConditionalOnMissingBean(name = "customWelcomeBean")
    public String defaultWelcomeBean() {
        System.out.println(">>> [AutoConfig] defaultWelcomeBean BEAN yaratildi");
        return "Welcome from default config!";
    }

    // ============ @ConditionalOnClass namoyishi ============
    // (Izohda: DataSource classpath da bormi? Agar spring-boot-starter-data-jpa qoshilgan bolsa -> Ha)

    // // Mana shu @ConditionalOnClass(DataSource.class) qanday ishlaydi:
    // @Bean
    // @ConditionalOnClass(name = "javax.sql.DataSource")
    // public String databaseAutoConfig() {
    //     return "DataSource class topildi -> Database auto-config ishladi!";
    // }
}
