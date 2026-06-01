package com.example.restapidemo.model;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

// @Repository = Springga "bu klass malumotlar bilan ishlaydi" deb aytadi
// Bu yerda malumotlar RAM da (List ichida) saqlanadi
// Haqiqiy loyihada bu JpaRepository bilan almashtiriladi
@Repository
public class UserRepository {

    // AtomicLong = bir vaqtning ozida kop thread chaqirsa ham xavfsiz
    private final AtomicLong counter = new AtomicLong(1);

    // Malumotlar RAM da saqlanadi (dastur qayta ishga tushsa tozalanadi)
    private final List<User> users = new ArrayList<>();

    // Konstruktorda 2 ta test user qoshiladi
    public UserRepository() {
        users.add(new User(counter.getAndIncrement(), "Ali", "ali@example.com", 25));
        users.add(new User(counter.getAndIncrement(), "Vali", "vali@example.com", 30));
    }

    // Barcha userlarni olish
    public List<User> findAll() {
        return users;
    }

    // ID boyicha bitta userni topish
    // Optional = null xatoliklaridan himoya qiladi
    public Optional<User> findById(Long id) {
        // stream() = listni qayta ishlash uchun
        // filter = id si togri kelganini top
        // findFirst = birinchisini ol
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    // Yangi user qoshish
    public User save(User user) {
        // Yangi ID yaratish (AtomicLong xavfsiz)
        user.setId(counter.getAndIncrement());
        users.add(user);
        return user;
    }

    // Userni yangilash (agar topilsa)
    public Optional<User> update(Long id, User updated) {
        // findById orqali userni topish
        return findById(id).map(existing -> {
            // Topilgan userni yangilash
            existing.setName(updated.getName());
            existing.setEmail(updated.getEmail());
            existing.setAge(updated.getAge());
            return existing;
        });
    }

    // Userni ochirish (agar topilsa)
    public boolean delete(Long id) {
        return users.removeIf(user -> user.getId().equals(id));
    }
}
