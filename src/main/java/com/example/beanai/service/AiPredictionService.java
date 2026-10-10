package com.example.beanai.service;

import com.example.beanai.dto.PredictionResultDTO;
import com.example.beanai.entity.DiagnosisHistory;
import com.example.beanai.entity.User;
import com.example.beanai.repository.DiagnosisHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service tích hợp phân tích sâu bệnh cây đậu qua mô hình AI best.pt
 */
@Service
public class AiPredictionService {

    private final DiagnosisHistoryRepository diagnosisHistoryRepository;
    private final ObjectMapper objectMapper;

    // Đường dẫn thư mục uploads và script AI
    private final String uploadDir = "uploads";
    private final String scriptPath = "ai-service/predict.py";
    private final String modelPath = "src/main/resources/static/best.pt";

    public AiPredictionService(DiagnosisHistoryRepository diagnosisHistoryRepository,
                               ObjectMapper objectMapper) {
        this.diagnosisHistoryRepository = diagnosisHistoryRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Phân tích ảnh lá đậu tải lên bằng mô hình best.pt và lưu lịch sử
     */
    @Transactional
    public PredictionResultDTO predictDisease(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) {
            return PredictionResultDTO.builder()
                    .success(false)
                    .error("Vui lòng chọn một tệp hình ảnh lá cây đậu hợp lệ.")
                    .build();
        }

        try {
            // 1. Tạo thư mục uploads nếu chưa tồn tại
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Lưu file với tên ngẫu nhiên tránh trùng lặp
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                fileExtension = ".jpg";
            }
            String uniqueFilename = UUID.randomUUID() + fileExtension;
            Path destinationPath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            // 3. Chuẩn bị đường dẫn model và script
            File modelFile = new File(modelPath);
            if (!modelFile.exists()) {
                // Thử tìm trong target classes
                modelFile = new File("target/classes/static/best.pt");
            }
            String absoluteModelPath = modelFile.getAbsolutePath();
            String absoluteImagePath = destinationPath.toFile().getAbsolutePath();
            String absoluteScriptPath = new File(scriptPath).getAbsolutePath();

            // 4. Thực thi tiến trình Python để chạy suy luận mô hình YOLO
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    absoluteScriptPath,
                    absoluteImagePath,
                    absoluteModelPath
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 5. Đọc kết quả JSON từ đầu ra của script
            StringBuilder outputBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line);
                }
            }

            int exitCode = process.waitFor();
            String jsonOutput = outputBuilder.toString().trim();

            if (exitCode != 0 || jsonOutput.isEmpty()) {
                return PredictionResultDTO.builder()
                        .success(false)
                        .error("Quá trình phân tích mô hình AI không thành công: " + jsonOutput)
                        .build();
            }

            // Tìm chuỗi JSON trong output (phòng trường hợp có log từ ultralytics)
            int jsonStart = jsonOutput.indexOf("{");
            int jsonEnd = jsonOutput.lastIndexOf("}");
            if (jsonStart == -1 || jsonEnd == -1) {
                return PredictionResultDTO.builder()
                        .success(false)
                        .error("Không nhận được dữ liệu JSON hợp lệ từ AI: " + jsonOutput)
                        .build();
            }
            jsonOutput = jsonOutput.substring(jsonStart, jsonEnd + 1);

            // 6. Chuyển đổi JSON sang DTO
            JsonNode rootNode = objectMapper.readTree(jsonOutput);
            boolean success = rootNode.path("success").asBoolean(false);

            if (!success) {
                String errorMsg = rootNode.path("error").asText("Lỗi không xác định từ mô hình AI.");
                return PredictionResultDTO.builder()
                        .success(false)
                        .error(errorMsg)
                        .build();
            }

            Integer classId = rootNode.path("class_id").asInt();
            String className = rootNode.path("class_name").asText();
            String vietnameseName = rootNode.path("vietnamese_name").asText();
            String scientificName = rootNode.path("scientific_name").asText();
            Double confidence = rootNode.path("confidence").asDouble();
            String severity = rootNode.path("severity").asText();
            String severityCode = rootNode.path("severity_code").asText();
            String symptoms = rootNode.path("symptoms").asText();
            String treatment = rootNode.path("treatment").asText();

            List<PredictionResultDTO.CandidateDTO> candidates = new ArrayList<>();
            JsonNode topCandNodes = rootNode.path("top_candidates");
            if (topCandNodes.isArray()) {
                for (JsonNode candNode : topCandNodes) {
                    candidates.add(PredictionResultDTO.CandidateDTO.builder()
                            .classId(candNode.path("class_id").asInt())
                            .className(candNode.path("class_name").asText())
                            .vietnameseName(candNode.path("vietnamese_name").asText())
                            .confidence(candNode.path("confidence").asDouble())
                            .build());
                }
            }

            String publicImageUrl = "/uploads/" + uniqueFilename;

            // 7. Lưu lại lịch sử chẩn đoán vào cơ sở dữ liệu nếu có user
            if (user != null) {
                DiagnosisHistory history = DiagnosisHistory.builder()
                        .user(user)
                        .imagePath(publicImageUrl)
                        .className(className)
                        .vietnameseName(vietnameseName)
                        .scientificName(scientificName)
                        .confidence(confidence)
                        .severity(severity)
                        .severityCode(severityCode)
                        .symptoms(symptoms)
                        .treatment(treatment)
                        .build();

                diagnosisHistoryRepository.save(history);
            }

            return PredictionResultDTO.builder()
                    .success(true)
                    .classId(classId)
                    .className(className)
                    .vietnameseName(vietnameseName)
                    .scientificName(scientificName)
                    .confidence(confidence)
                    .severity(severity)
                    .severityCode(severityCode)
                    .symptoms(symptoms)
                    .treatment(treatment)
                    .imageUrl(publicImageUrl)
                    .topCandidates(candidates)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return PredictionResultDTO.builder()
                    .success(false)
                    .error("Đã xảy ra lỗi trong quá trình xử lý ảnh: " + e.getMessage())
                    .build();
        }
    }
}
