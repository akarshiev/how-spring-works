package org.example.springcore.WithAutoConfig;

import org.springframework.stereotype.Component;

@Component
public class Store {
    private final Item item;

    // Autowired yozish shart emas (agar bitta konstruktor bo'lsa)
    public Store(Item item) {
        this.item = item;
    }

    public void print() {
        System.out.println(item.getName());
    }
}