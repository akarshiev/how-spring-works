package com.example.restapidemo.controller;

import com.example.restapidemo.model.User;
import com.example.restapidemo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController = bu klass REST API ga javob beradi
// @RequestMapping("/api/users") = barcha URL lar "/api/users" bilan boshlanadi
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Constructor Injection - Spring Service ni avtomatik beradi
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users -> hamma userlarni olish
    // curl http://localhost:8080/api/users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    // GET /api/users/{id} -> bitta userni olish
    // @PathVariable = URL dan {id} qiymatini olish
    // curl http://localhost:8080/api/users/1
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    // POST /api/users -> yangi user yaratish
    // @RequestBody = HTTP request body sidagi JSON ni User obektiga aylantirish
    // curl -X POST http://localhost:8080/api/users \
    //   -H "Content-Type: application/json" \
    //   -d '{"name":"Soli","email":"soli@example.com","age":28}'
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userService.create(user);
        // 201 = Created (resurs muvaffaqiyatli yaratildi)
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/users/{id} -> userni yangilash
    // curl -X PUT http://localhost:8080/api/users/1 \
    //   -H "Content-Type: application/json" \
    //   -d '{"name":"Aliy","email":"aliy@example.com","age":26}'
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.update(id, user);
    }

    // DELETE /api/users/{id} -> userni ochirish
    // curl -X DELETE http://localhost:8080/api/users/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        // 204 = No Content (muvaffaqiyatli ochirildi, javob yoq)
        return ResponseEntity.noContent().build();
    }
}
