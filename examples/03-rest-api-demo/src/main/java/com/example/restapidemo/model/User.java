package com.example.restapidemo.model;

// User - bu bizning malumot modelimiz
// Hech qanday JPA annotation lar yo'q, chunk bu RAM da ishlaydi
public class User {

    // private = bu maydonga faqat shu klass ichidan kirish mumkin
    private Long id;       // Foydalanuvchi ID si (unique)
    private String name;   // Foydalanuvchi ismi
    private String email;  // Foydalanuvchi emaili
    private int age;       // Foydalanuvchi yoshi

    // Bo'sh constructor - Jackson JSON ni obektga aylantirish uchun kerak
    public User() {}

    // To'liq constructor - yangi User yaratish uchun
    public User(Long id, String name, String email, int age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    // Getter va Setter lar - Jackson va boshqa kodlar uchun kerak
    // Getter = maydon qiymatini olish
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
