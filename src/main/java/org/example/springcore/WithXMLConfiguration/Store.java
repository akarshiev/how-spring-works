package org.example.springcore.WithXMLConfiguration;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Store {
    private Item item;

    public void print() {
        System.out.println(item.getName() + " - " + item.getPrice());
    }
}
