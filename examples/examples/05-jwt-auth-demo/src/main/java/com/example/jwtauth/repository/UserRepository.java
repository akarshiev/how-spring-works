package com.example.jwtauth.repository;

import com.example.jwtauth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository = save(), findById(), findAll(), delete() metodlari oz-ozidan keladi
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE username = ?
    Optional<User> findByUsername(String username);

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT COUNT(*) FROM users WHERE username = ?
    boolean existsByUsername(String username);

    // SELECT COUNT(*) FROM users WHERE email = ?
    boolean existsByEmail(String email);
}
