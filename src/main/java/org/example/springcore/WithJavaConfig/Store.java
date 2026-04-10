package org.example.springcore.WithJavaConfig;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    private Item item;

    public void print() {
        System.out.println(item.getName() + " - " + item.getPrice());
    }
}
