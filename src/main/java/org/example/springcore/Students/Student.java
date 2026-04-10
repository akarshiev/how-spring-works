package org.example.springcore.Students;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private String name;
    private List<String> subjects;
    private Map<String, Integer> grades;

    public void print() {
        System.out.println("Talaba: " + name);
        System.out.println("--- Fanlar ---");
        subjects.forEach(s -> System.out.println("- " + s));
        System.out.println("--- Baholar ---");
        grades.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}