package com.example.restapidemo.service;

// UserNotFoundException = custom xatolik klassi
// RuntimeException dan voris oladi (run time da ishlaydi)
// Bu xatolik @ControllerAdvice da ushlanib, 404 qaytaradi
public class UserNotFoundException extends RuntimeException {

    // Konstruktor: id ni olib, tushunarli xabar yaratadi
    public UserNotFoundException(Long id) {
        // super() -> RuntimeException ning konstruktorini chaqiradi
        super("User topilmadi, id: " + id);
    }
}
