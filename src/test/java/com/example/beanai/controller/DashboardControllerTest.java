package com.example.beanai.controller;

import com.example.beanai.entity.Role;
import com.example.beanai.entity.User;
import com.example.beanai.repository.DiagnosisHistoryRepository;
import com.example.beanai.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JUnit 5 - Kiểm thử Controller DashboardController (Trang Admin hiển thị tài khoản)")
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private DiagnosisHistoryRepository diagnosisHistoryRepository;

    @InjectMocks
    private DashboardController dashboardController;

    private User admin;
    private User newFarmer;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setViewResolvers(viewResolver)
                .build();

        admin = User.builder().id(1L).fullName("Admin").email("admin@beanai.com")
                .createdAt(LocalDateTime.now().minusDays(10))
                .roles(Set.of(new Role(1L, "ROLE_ADMIN"))).build();
        newFarmer = User.builder().id(2L).fullName("Nông dân mới").email("new@gmail.com")
                .createdAt(LocalDateTime.now())
                .roles(Set.of(new Role(2L, "ROLE_FARMER"))).build();
    }

    @Test
    @DisplayName("Test 1: GET /admin/dashboard -> model chứa danh sách user thật, người mới đăng ký đứng đầu")
    void adminDashboard_showsRegisteredUsers() throws Exception {
        when(userService.findByEmail("admin@beanai.com")).thenReturn(Optional.of(admin));
        when(userService.findAllUsers()).thenReturn(List.of(newFarmer, admin));
        when(userService.countUsers()).thenReturn(2L);
        when(diagnosisHistoryRepository.count()).thenReturn(7L);

        var auth = new UsernamePasswordAuthenticationToken("admin@beanai.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        mockMvc.perform(get("/admin/dashboard").principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("currentUser", admin))
                .andExpect(model().attribute("users", hasSize(2)))
                .andExpect(model().attribute("users", contains(newFarmer, admin)))
                .andExpect(model().attribute("users", hasItem(hasProperty("email", is("new@gmail.com")))))
                .andExpect(model().attribute("totalUsers", 2L))
                .andExpect(model().attribute("totalDiagnoses", 7L));
    }

    @Test
    @DisplayName("Test 2: GET /admin/dashboard khi chưa có user -> danh sách rỗng, tổng = 0")
    void adminDashboard_emptyUsers() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of());
        when(userService.countUsers()).thenReturn(0L);
        when(diagnosisHistoryRepository.count()).thenReturn(0L);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("users", empty()))
                .andExpect(model().attribute("totalUsers", 0L))
                .andExpect(model().attributeDoesNotExist("currentUser"));
    }
}
