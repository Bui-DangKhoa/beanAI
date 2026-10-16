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
     * API tải lên hình ảnh lá cây họ đậu và thực hiện suy luận bệnh học qua mô hình AI (best.pt)
     *
     * @param image          Tệp hình ảnh do người dùng tải lên (JPG, PNG, WEBP, tối đa 25MB).
     * @param authentication Thông tin người dùng đăng nhập từ Spring Security (nếu đã đăng nhập).
     * @return ResponseEntity chứa đối tượng PredictionResultDTO với thông tin chẩn đoán và phác đồ điều trị.
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

        // Gọi Service xử lý lưu ảnh, gọi mô hình AI suy luận và lưu lịch sử chẩn đoán
        PredictionResultDTO result = aiPredictionService.predictDisease(image, currentUser);
        return ResponseEntity.ok(result);
    }

    /**
     * API tra cứu danh mục toàn bộ 11 loại sâu bệnh và phác đồ điều trị chuẩn Nông nghiệp
     *
     * @return ResponseEntity chứa danh sách 11 lớp bệnh kèm triệu chứng, sinh thái và phác đồ mẫu.
     */
    @GetMapping("/diseases")
    public ResponseEntity<?> getAllDiseases() {
        return ResponseEntity.ok(aiPredictionService.getAllDiseases());
    }
}

