package com.example.restapidemo.service;

import com.example.restapidemo.model.User;
import com.example.restapidemo.model.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// @Service = bu klass biznes logika bilan shugullanadi
// Controller dan kelgan malumotni tekshiradi, Repository ga yuboradi
@Service
public class UserService {

    // Repository ga bogliqlik (Dependency Injection)
    private final UserRepository repository;

    // Constructor Injection - Spring avtomatik repository ni beradi
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // Hamma userlarni olish
    public List<User> findAll() {
        return repository.findAll();
    }

    // Bitta userni ID boyicha topish
    // Agar topilmasa, xatolik chiqarish
    public User findById(Long id) {
        return repository.findById(id)
                // .orElseThrow() = agar topilmasa, exception tashla
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    // Yangi user yaratish
    public User create(User user) {
        // Email tekshirish (null yoki bo'sh bolmasligi kerak)
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email bosh bolmasligi kerak");
        }
        return repository.save(user);
    }

    // Userni yangilash
    public User update(Long id, User user) {
        return repository.update(id, user)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    // Userni ochirish
    public void delete(Long id) {
        if (!repository.delete(id)) {
            throw new UserNotFoundException(id);
        }
    }
}
