package com.example.springbootdemo.config;

// @ConfigurationProperties = application.properties dagi "app." bilan boshlangan
// hamma property larni avtomatik ravishda shu klassning maydonlariga yuklaydi
// Spring Boot ni ichki mexanizmi: Environment dan property larni oqib, setter lar orqali yuklaydi
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    // application.properties dan:
    // app.name="Spring Boot Demo" -> name maydoniga yuklanadi
    // app.version="1.0.0"         -> version maydoniga yuklanadi
    private String name;
    private String version;
    private String description;

    // Ichki klass: "app.features" bilan boshlangan property lar
    // app.features.greeting-enabled -> features.greetingEnabled ga yuklanadi
    private Features features = new Features();

    // Getter va Setter lar (Spring @ConfigurationProperties setter orqali yuklaydi)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Features getFeatures() { return features; }
    public void setFeatures(Features features) { this.features = features; }

    // Ichki klass: app.features.* property lari uchun
    public static class Features {
        private boolean greetingEnabled;
        private boolean detailsEnabled;

        public boolean isGreetingEnabled() { return greetingEnabled; }
        public void setGreetingEnabled(boolean greetingEnabled) {
            this.greetingEnabled = greetingEnabled;
        }

        public boolean isDetailsEnabled() { return detailsEnabled; }
        public void setDetailsEnabled(boolean detailsEnabled) {
            this.detailsEnabled = detailsEnabled;
        }
    }
}
