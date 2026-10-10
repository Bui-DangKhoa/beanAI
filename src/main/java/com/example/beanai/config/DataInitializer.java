package com.example.beanai.config;

import com.example.beanai.entity.Role;
import com.example.beanai.entity.User;
import com.example.beanai.repository.RoleRepository;
import com.example.beanai.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

/**
 * Tự động khởi tạo dữ liệu mẫu (Roles và tài khoản Admin mặc định) khi ứng dụng khởi chạy
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection
    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Khởi tạo các Role nếu chưa tồn tại
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        Role farmerRole = roleRepository.findByName("ROLE_FARMER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_FARMER")));

        // 2. Khởi tạo tài khoản Quản trị viên (Admin) mặc định nếu chưa tồn tại
        final String adminEmail = "admin@beanai.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User defaultAdmin = User.builder()
                    .fullName("Quản trị viên BeanAI")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@123"))
                    .phone("0987654321")
                    .enabled(true)
                    .roles(new HashSet<>(Collections.singletonList(adminRole)))
                    .build();

            userRepository.save(defaultAdmin);
            System.out.println(">>> Đã khởi tạo tài khoản Admin mặc định: " + adminEmail + " / Admin@123");
        }
    }
}
