package com.example.beanai.controller;

import com.example.beanai.entity.User;
import com.example.beanai.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

/**
 * Controller xử lý màn hình làm việc sau đăng nhập:
 * - /dashboard: Dành cho bà con nông dân, hợp tác xã (FARMER)
 * - /admin/dashboard: Dành cho ban quản trị hệ thống (ROLE_ADMIN)
 */
@Controller
public class DashboardController {

    private final UserService userService;
    private final com.example.beanai.repository.DiagnosisHistoryRepository diagnosisHistoryRepository;

    // Constructor injection
    public DashboardController(UserService userService,
                               com.example.beanai.repository.DiagnosisHistoryRepository diagnosisHistoryRepository) {
        this.userService = userService;
        this.diagnosisHistoryRepository = diagnosisHistoryRepository;
    }

    /**
     * Bàn làm việc của Nông dân (Chẩn đoán sâu bệnh, lịch sử quét cây đậu)
     */
    @GetMapping("/dashboard")
    public String farmerDashboard(Authentication authentication, Model model) {
        if (authentication != null) {
            String email = authentication.getName();
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                model.addAttribute("currentUser", user);
                model.addAttribute("diagnosisList", diagnosisHistoryRepository.findByUserOrderByCreatedAtDesc(user));
                model.addAttribute("totalScans", diagnosisHistoryRepository.countByUser(user));
            }
        }
        model.addAttribute("pageTitle", "Bàn làm việc Nông dân - BeanAI");
        return "dashboard";
    }

    /**
     * Bảng điều khiển Quản trị viên (Quản lý tài khoản, danh mục sâu bệnh, thống kê AI)
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        if (authentication != null) {
            String email = authentication.getName();
            Optional<User> userOptional = userService.findByEmail(email);
            userOptional.ifPresent(user -> model.addAttribute("currentUser", user));
        }
        model.addAttribute("pageTitle", "Trung tâm quản trị Admin - BeanAI");
        return "admin/dashboard";
    }
}
