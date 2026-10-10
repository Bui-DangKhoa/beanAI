package com.example.beanai.controller;

import com.example.beanai.dto.PredictionResultDTO;
import com.example.beanai.entity.User;
import com.example.beanai.service.AiPredictionService;
import com.example.beanai.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Controller cung cấp API phân tích hình ảnh sâu bệnh bằng mô hình AI best.pt
 */
@RestController
@RequestMapping("/api/ai")
public class AiPredictionController {

    private final AiPredictionService aiPredictionService;
    private final UserService userService;

    // Constructor injection
    public AiPredictionController(AiPredictionService aiPredictionService,
                                  UserService userService) {
        this.aiPredictionService = aiPredictionService;
        this.userService = userService;
    }

    /**
     * API tải ảnh lá đậu và trả về kết quả dự đoán từ model best.pt
     */
    @PostMapping("/predict")
    public ResponseEntity<PredictionResultDTO> predict(@RequestParam("image") MultipartFile image,
                                                       Authentication authentication) {
        User currentUser = null;
        if (authentication != null) {
            String email = authentication.getName();
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isPresent()) {
                currentUser = userOptional.get();
            }
        }

        PredictionResultDTO result = aiPredictionService.predictDisease(image, currentUser);
        return ResponseEntity.ok(result);
    }
}
