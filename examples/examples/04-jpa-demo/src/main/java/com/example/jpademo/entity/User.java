package com.example.jpademo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// @Entity = bu klass malumotlar bazasidagi jadvalga togri keladi
// @Table(name = "users") -> jadval nomi "users"
@Entity
@Table(name = "users")
public class User {

    // @Id = primary key (asosiy kalit)
    // @GeneratedValue = ID avtomatik yaratilsin
    // IDENTITY = database ga auto-increment ishlatilsin
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column = jadval ustunini sozlash
    // nullable = false -> bu ustun bosh bolmasligi kerak
    // length = 100 -> maksimal 100 belgi
    @Column(nullable = false, length = 100)
    private String name;

    // unique = true -> bu qiymat takrorlanmasligi kerak
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    private int age;

    // @Enumerated(STRING) -> enum nomi bilan saqlansin (USER, ADMIN)
    @Enumerated(EnumType.STRING)
    private Role role;

    // updatable = false -> bir marta yoziladi, keyin ozgartirib bolmaydi
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // @PrePersist = bu entity birinchi marta saqlashdan oldin ishlaydi
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // @PreUpdate = bu entity har safar yangilashdan oldin ishlaydi
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getter va Setter lar
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
