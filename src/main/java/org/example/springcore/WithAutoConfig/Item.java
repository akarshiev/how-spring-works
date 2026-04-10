package org.example.springcore.WithAutoConfig;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Item {
    private String name = "Default Item";
    private double price = 100.0;
}