package org.example.springcore.WithAutoConfig.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ComponentScan(basePackages = "org.example.springcore.WithAutoConfig")
public class AppConfig {
}
