package com.example.jpademo.service;

import com.example.jpademo.entity.User;
import com.example.jpademo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service = bu klass biznes logika bilan shugullanadi
@Service
public class UserService {

    private final UserRepository repository;

    // Constructor Injection
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // @Transactional(readOnly = true) = oqish uchun (tezroq ishlaydi)
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User topilmadi: " + id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email topilmadi: " + email));
    }

    // @Transactional = bu metod ichidagi hammasi bir tranzaksiya
    // Agar xatolik chiqsa -> ROLLBACK (hech narsa saqlanmaydi)
    // Agar hammasi yaxshi -> COMMIT (saqlanadi)
    @Transactional
    public User create(User user) {
        // Email unique ekanligini tekshirish
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Bu email allaqachon mavjud: " + user.getEmail());
        }
        return repository.save(user);
    }

    @Transactional
    public User update(Long id, User updated) {
        User user = findById(id);
        user.setName(updated.getName());
        user.setEmail(updated.getEmail());
        user.setAge(updated.getAge());
        user.setPhoneNumber(updated.getPhoneNumber());
        // save() = agar ID bor bolsa, UPDATE qiladi
        return repository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("User topilmadi: " + id);
        }
        repository.deleteById(id);
    }
}
