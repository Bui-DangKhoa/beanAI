package com.example.beanai.repository;

import com.example.beanai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác với bảng users
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Lấy toàn bộ người dùng, mới đăng ký nhất lên đầu (dùng cho trang Admin)
    List<User> findAllByOrderByCreatedAtDesc();
}
