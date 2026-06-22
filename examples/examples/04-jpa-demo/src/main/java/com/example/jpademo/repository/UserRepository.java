package com.example.jpademo.repository;

import com.example.jpademo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// JpaRepository<User, Long> = User entity, Long = ID tipi
// JpaRepository ichida: save(), findById(), findAll(), delete() va boshqalar bor
public interface UserRepository extends JpaRepository<User, Long> {

    // Query metodlar - Spring Data JPA metod nomini oqib, SQL yozadi

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT * FROM users WHERE email = ? AND name = ?
    Optional<User> findByEmailAndName(String email, String name);

    // SELECT * FROM users WHERE age > ?
    List<User> findByAgeGreaterThan(int age);

    // SELECT * FROM users WHERE name LIKE '%keyword%'
    List<User> findByNameContainingIgnoreCase(String keyword);

    // SELECT COUNT(*) FROM users WHERE role = ?
    long countByRole(User.Role role);

    // SELECT * FROM users WHERE created_at > ?
    List<User> findByCreatedAtAfter(java.time.LocalDateTime date);

    // @Query - o'zingiz JPQL yozishingiz
    @Query("SELECT u FROM User u WHERE u.age BETWEEN :min AND :max")
    List<User> findUsersByAgeRange(@Param("min") int min, @Param("max") int max);

    // @Query - Native SQL (jadval nomi bilan)
    @Query(value = "SELECT * FROM users WHERE email LIKE %:domain%", nativeQuery = true)
    List<User> findUsersByEmailDomain(@Param("domain") String domain);
}
