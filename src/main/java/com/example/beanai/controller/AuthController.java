package com.example.beanai.controller;

import com.example.beanai.dto.RegisterDTO;
import com.example.beanai.exception.EmailAlreadyExistsException;
import com.example.beanai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller phụ trách xác thực: Đăng nhập (/login) và Đăng ký (/register)
 */
@Controller
public class AuthController {

    private final UserService userService;

    // Constructor injection
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Hiển thị giao diện trang Đăng nhập
     */
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("pageTitle", "Đăng nhập - BeanAI");
        return "auth/login";
    }

    /**
     * Hiển thị giao diện trang Đăng ký tài khoản
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterDTO());
        }
        model.addAttribute("pageTitle", "Đăng ký tài khoản - BeanAI");
        return "auth/register";
    }

    /**
     * Xử lý dữ liệu đăng ký gửi lên từ form
     */
    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        // 1. Kiểm tra mật khẩu xác nhận có khớp không
        if (registerDTO.getPassword() != null && registerDTO.getConfirmPassword() != null) {
            if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp");
            }
        }

        // 2. Kiểm tra email đã tồn tại trong hệ thống chưa
        if (registerDTO.getEmail() != null && !registerDTO.getEmail().isBlank()) {
            if (userService.existsByEmail(registerDTO.getEmail())) {
                bindingResult.rejectValue("email", "error.email", "Email này đã được sử dụng. Vui lòng chọn email khác.");
            }
        }

        // 3. Nếu có lỗi validation, trả về lại trang đăng ký và giữ nguyên dữ liệu đã nhập
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Đăng ký tài khoản - BeanAI");
            return "auth/register";
        }

        // 4. Thực hiện đăng ký thông qua UserService
        try {
            userService.registerUser(registerDTO);
        } catch (EmailAlreadyExistsException e) {
            bindingResult.rejectValue("email", "error.email", e.getMessage());
            model.addAttribute("pageTitle", "Đăng ký tài khoản - BeanAI");
            return "auth/register";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại!");
            model.addAttribute("pageTitle", "Đăng ký tài khoản - BeanAI");
            return "auth/register";
        }

        // 5. Đăng ký thành công -> redirect sang /login?registered kèm flash attribute thông báo
        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        return "redirect:/login?registered";
    }
}
