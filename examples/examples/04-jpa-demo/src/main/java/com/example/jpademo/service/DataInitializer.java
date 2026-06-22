package com.example.jpademo.service;

import com.example.jpademo.entity.Role;
import com.example.jpademo.entity.User;
import com.example.jpademo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// @Component = Spring bu klassi avtomatik topib, ishga tushiradi
// CommandLineRunner = ilova ishga tushganda run() metodini bajaradi
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository repository;

    public DataInitializer(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        // Agar database bosh bolsa, test malumotlar qoshish
        if (repository.count() == 0) {
            User ali = new User();
            ali.setName("Ali");
            ali.setEmail("ali@example.com");
            ali.setPassword("123");
            ali.setAge(25);
            ali.setRole(Role.ADMIN);
            repository.save(ali);

            User vali = new User();
            vali.setName("Vali");
            vali.setEmail("vali@example.com");
            vali.setPassword("456");
            vali.setAge(30);
            vali.setRole(Role.USER);
            repository.save(vali);

            System.out.println("Test malumotlar qoshildi!");
        }
    }
}
