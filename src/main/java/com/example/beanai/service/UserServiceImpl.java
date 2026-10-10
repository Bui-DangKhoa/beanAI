package com.example.beanai.service;

import com.example.beanai.dto.RegisterDTO;
import com.example.beanai.entity.Role;
import com.example.beanai.entity.User;
import com.example.beanai.exception.EmailAlreadyExistsException;
import com.example.beanai.repository.RoleRepository;
import com.example.beanai.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

/**
 * Lớp triển khai nghiệp vụ người dùng, sử dụng Constructor Injection
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection thay vì @Autowired trên field
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(RegisterDTO registerDTO) {
        // Kiểm tra tính duy nhất của email
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email '" + registerDTO.getEmail() + "' đã tồn tại trong hệ thống.");
        }

        // Kiểm tra mật khẩu khớp nhau
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu và xác nhận mật khẩu không khớp.");
        }

        // Lấy hoặc tạo vai trò mặc định ROLE_FARMER
        Role farmerRole = roleRepository.findByName("ROLE_FARMER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_FARMER")));

        // Khởi tạo đối tượng User mới
        User user = User.builder()
                .fullName(registerDTO.getFullName().trim())
                .email(registerDTO.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .phone(registerDTO.getPhone().trim())
                .enabled(true)
                .roles(new HashSet<>(Collections.singletonList(farmerRole)))
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }
}
