package com.example.restapidemo.service;

// @ControllerAdvice = "Men hamma controller larni xatoliklardan himoya qilaman"
// @ExceptionHandler = "Agar UserNotFoundException chiqsa, bu metod ishlaydi"
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice  // Global xatolik ushlagich
public class GlobalExceptionHandler {

    // UserNotFoundException ushlanganda 404 qaytarish
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        // Chiroyli xatolik javobi yaratish
        // Map = kalit-qiymat juftliklari, JSON ga aylanadi
        Map<String, Object> body = Map.of(
                "status", 404,
                "error", "Not Found",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // IllegalArgumentException ushlanganda 400 qaytarish
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, Object> body = Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
