package com.example.beanai.controller;

import com.example.beanai.dto.RegisterDTO;
import com.example.beanai.exception.EmailAlreadyExistsException;
import com.example.beanai.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JUnit 5 - Kiểm thử Controller AuthController (Đăng ký tài khoản)")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    @DisplayName("Test 1: GET /register -> hiển thị form với RegisterDTO rỗng")
    void showRegisterForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registerDTO"));
    }

    @Test
    @DisplayName("Test 2: POST /register hợp lệ -> gọi registerUser và redirect /login?registered")
    void processRegister_success() throws Exception {
        when(userService.existsByEmail("new@gmail.com")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("fullName", "Nguyễn Văn B")
                        .param("email", "new@gmail.com")
                        .param("phone", "0912345678")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).registerUser(any(RegisterDTO.class));
    }

    @Test
    @DisplayName("Test 3: POST /register thiếu dữ liệu / sai định dạng -> trả lại form kèm lỗi, không lưu")
    void processRegister_validationErrors() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "")
                        .param("email", "not-an-email")
                        .param("phone", "123")
                        .param("password", "12")
                        .param("confirmPassword", "12"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerDTO",
                        "fullName", "email", "phone", "password"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("Test 4: POST /register mật khẩu xác nhận không khớp -> lỗi confirmPassword")
    void processRegister_passwordMismatch() throws Exception {
        when(userService.existsByEmail(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("fullName", "Nguyễn Văn B")
                        .param("email", "new@gmail.com")
                        .param("phone", "0912345678")
                        .param("password", "secret123")
                        .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerDTO", "confirmPassword"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("Test 5: POST /register email đã tồn tại -> lỗi trường email, không lưu")
    void processRegister_emailExists() throws Exception {
        when(userService.existsByEmail("used@gmail.com")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("fullName", "Nguyễn Văn B")
                        .param("email", "used@gmail.com")
                        .param("phone", "0912345678")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerDTO", "email"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("Test 6: Service ném EmailAlreadyExistsException (race condition) -> lỗi trường email")
    void processRegister_serviceThrowsEmailExists() throws Exception {
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(userService.registerUser(any())).thenThrow(new EmailAlreadyExistsException("Email đã tồn tại"));

        mockMvc.perform(post("/register")
                        .param("fullName", "Nguyễn Văn B")
                        .param("email", "new@gmail.com")
                        .param("phone", "0912345678")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerDTO", "email"));
    }

    @Test
    @DisplayName("Test 7: Service ném lỗi bất ngờ -> hiển thị errorMessage chung")
    void processRegister_unexpectedError() throws Exception {
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(userService.registerUser(any())).thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(post("/register")
                        .param("fullName", "Nguyễn Văn B")
                        .param("email", "new@gmail.com")
                        .param("phone", "0912345678")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
