package com.example.beanai.service;

import com.example.beanai.dto.RegisterDTO;
import com.example.beanai.entity.User;

import java.util.Optional;

/**
 * Interface định nghĩa các nghiệp vụ quản lý người dùng
 */
public interface UserService {

    /**
     * Đăng ký người dùng mới với vai trò ROLE_FARMER mặc định
     */
    User registerUser(RegisterDTO registerDTO);

    /**
     * Kiểm tra xem email đã tồn tại hay chưa
     */
    boolean existsByEmail(String email);

    /**
     * Tìm kiếm người dùng theo email
     */
    Optional<User> findByEmail(String email);
}
