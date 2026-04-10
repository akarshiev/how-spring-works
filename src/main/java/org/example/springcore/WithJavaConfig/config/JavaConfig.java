package org.example.springcore.WithJavaConfig.config;

import org.example.springcore.WithJavaConfig.Item;
import org.example.springcore.WithJavaConfig.Store;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaConfig {

    @Bean
    public Item item() {
        return new Item("MacBook", 1500.0);
    }

    @Bean
    public Store store() {
        return new Store(item());
    }
}