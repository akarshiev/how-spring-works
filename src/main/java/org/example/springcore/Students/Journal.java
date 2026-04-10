package org.example.springcore.Students;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class Journal {
    private List<String> subjects;
    private Map<String, Integer> grades;

    public void print() {
        System.out.println(subjects);
        System.out.println(grades);
    }
}