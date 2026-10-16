package com.example.beanai.service;

import com.example.beanai.dto.RegisterDTO;
import com.example.beanai.entity.User;

import java.util.List;
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

    /**
     * Lấy danh sách toàn bộ người dùng (mới nhất trước) cho trang quản trị
     */
    List<User> findAllUsers();

    /**
     * Đếm tổng số người dùng trong hệ thống
     */
    long countUsers();
}
