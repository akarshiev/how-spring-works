package com.example.jpademo.controller;

import com.example.jpademo.entity.User;
import com.example.jpademo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// UserController = REST API orqali User malumotlarini boshqarish
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users -> hamma userlar
    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    // GET /api/users/5 -> id=5 bolgan user
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    // GET /api/users/email/{email} -> email boyicha qidirish
    @GetMapping("/email/{email}")
    public User getByEmail(@PathVariable String email) {
        return userService.findByEmail(email);
    }

    // POST /api/users -> yangi user yaratish
    // Body: {"name":"Ali","email":"ali@example.com","password":"123","age":25}
    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        User saved = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/users/5 -> userni yangilash
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        return userService.update(id, user);
    }

    // DELETE /api/users/5 -> userni ochirish
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
