package com.example.beanai.service;

import com.example.beanai.dto.RegisterDTO;
import com.example.beanai.entity.Role;
import com.example.beanai.entity.User;
import com.example.beanai.exception.EmailAlreadyExistsException;
import com.example.beanai.repository.RoleRepository;
import com.example.beanai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JUnit 5 - Kiểm thử nghiệp vụ UserServiceImpl (Đăng ký & Danh sách tài khoản)")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = RegisterDTO.builder()
                .fullName("  Nguyễn Văn A  ")
                .email("  NongDan@Gmail.com ")
                .phone(" 0912345678 ")
                .password("secret123")
                .confirmPassword("secret123")
                .build();
    }

    @Test
    @DisplayName("Test 1: Đăng ký hợp lệ -> lưu User với email chuẩn hóa, mật khẩu mã hóa, ROLE_FARMER, enabled")
    void registerUser_success() {
        Role farmer = new Role(1L, "ROLE_FARMER");
        when(userRepository.existsByEmail("nongdan@gmail.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_FARMER")).thenReturn(Optional.of(farmer));
        when(passwordEncoder.encode("secret123")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(validDto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User u = captor.getValue();
        assertThat(u.getFullName()).isEqualTo("Nguyễn Văn A");
        assertThat(u.getEmail()).isEqualTo("nongdan@gmail.com");
        assertThat(u.getPhone()).isEqualTo("0912345678");
        assertThat(u.getPassword()).isEqualTo("ENCODED");
        assertThat(u.isEnabled()).isTrue();
        assertThat(u.getRoles()).extracting(Role::getName).containsExactly("ROLE_FARMER");
        assertThat(saved).isSameAs(u);
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 2: Chưa có ROLE_FARMER trong CSDL -> tự động tạo role mới")
    void registerUser_createsRoleWhenMissing() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_FARMER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(validDto);

        verify(roleRepository).save(argThat(r -> "ROLE_FARMER".equals(r.getName())));
        assertThat(saved.getRoles()).extracting(Role::getName).containsExactly("ROLE_FARMER");
    }

    @Test
    @DisplayName("Test 3: Email đã tồn tại (khác hoa/thường) -> ném EmailAlreadyExistsException, không lưu")
    void registerUser_duplicateEmailCaseInsensitive() {
        when(userRepository.existsByEmail("nongdan@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(validDto))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 4: Mật khẩu xác nhận không khớp -> ném IllegalArgumentException, không lưu")
    void registerUser_passwordMismatch() {
        validDto.setConfirmPassword("other");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.registerUser(validDto))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 5: findAllUsers -> trả về danh sách sắp xếp theo ngày tạo giảm dần")
    void findAllUsers_returnsNewestFirst() {
        User newUser = User.builder().id(2L).email("new@x.com").build();
        User oldUser = User.builder().id(1L).email("old@x.com").build();
        when(userRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(newUser, oldUser));

        List<User> result = userService.findAllUsers();

        assertThat(result).containsExactly(newUser, oldUser);
        verify(userRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Test 6: countUsers -> trả về tổng số tài khoản từ repository")
    void countUsers_delegates() {
        when(userRepository.count()).thenReturn(5L);
        assertThat(userService.countUsers()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Test 7: existsByEmail / findByEmail -> chuẩn hóa email trước khi truy vấn")
    void existsAndFind_normalizeEmail() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(new User()));

        assertThat(userService.existsByEmail("  A@B.com ")).isTrue();
        assertThat(userService.findByEmail(" A@b.COM")).isPresent();
    }
}
