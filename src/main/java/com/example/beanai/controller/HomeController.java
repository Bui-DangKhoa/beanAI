package com.example.beanai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller xử lý các trang công khai (Trang chủ giới thiệu hệ thống BeanAI)
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "BeanAI - Nhận diện sâu bệnh trên cây đậu thông minh");
        return "home";
    }
}
