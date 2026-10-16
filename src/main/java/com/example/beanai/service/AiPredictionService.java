package com.example.beanai.service;

import com.example.beanai.dto.PredictionResultDTO;
import com.example.beanai.entity.DiagnosisHistory;
import com.example.beanai.entity.User;
import com.example.beanai.repository.DiagnosisHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.TimeUnit;

/**
 * Service tích hợp phân tích sâu bệnh cây đậu qua mô hình AI best.pt
 * và gợi ý phác đồ điều trị chuyên sâu theo tiêu chuẩn nông nghiệp.
 */
@Service
public class AiPredictionService {

    private static final Logger log = LoggerFactory.getLogger(AiPredictionService.class);

    private final DiagnosisHistoryRepository diagnosisHistoryRepository;
    private final ObjectMapper objectMapper;

    @Value("${beanai.python.path:}")
    private String configuredPythonPath;

    @Value("${beanai.upload.dir:uploads}")
    private String uploadDir;

    @Value("${beanai.script.path:ai-service/predict.py}")
    private String scriptPath;

    @Value("${beanai.model.path:src/main/resources/static/best.pt}")
    private String modelPath;

    // Cache python executable sau lần kiểm tra đầu tiên
    private volatile String cachedPythonExecutable = null;

    public AiPredictionService(DiagnosisHistoryRepository diagnosisHistoryRepository,
                               ObjectMapper objectMapper) {
        this.diagnosisHistoryRepository = diagnosisHistoryRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Tự động phát hiện trình thông dịch Python hoạt động được trên hệ thống (Windows / Linux)
     */
    public synchronized String resolvePythonExecutable() {
        if (cachedPythonExecutable != null) {
            return cachedPythonExecutable;
        }

        List<String> candidates = new ArrayList<>();
        if (configuredPythonPath != null && !configuredPythonPath.trim().isEmpty()) {
            candidates.add(configuredPythonPath.trim());
        }
        // Các lệnh python tiêu chuẩn trên môi trường hệ thống (Windows / Linux / macOS / Docker)
        candidates.add("python");
        candidates.add("python3");
        candidates.add("py");

        for (String candidate : candidates) {
            try {
                Process process = new ProcessBuilder(candidate, "--version").start();
                boolean finished = process.waitFor(3, TimeUnit.SECONDS);
                if (finished && process.exitValue() == 0) {
                    cachedPythonExecutable = candidate;
                    log.info(">>> BeanAI đã phát hiện và kích hoạt Python executable: {}", candidate);
                    return cachedPythonExecutable;
                }
            } catch (Exception ignored) {
                // Tiếp tục thử candidate tiếp theo
            }
        }

        // Mặc định fallback
        cachedPythonExecutable = "python";
        return cachedPythonExecutable;
    }

    /**
     * Phân tích ảnh lá đậu tải lên bằng mô hình best.pt và lưu lịch sử chẩn đoán
     */
    @Transactional
    public PredictionResultDTO predictDisease(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) {
            return PredictionResultDTO.builder()
                    .success(false)
                    .errorTitle("Tệp hình ảnh không hợp lệ")
                    .errorLocation("Cổng tiếp nhận tệp của máy chủ (Web Server Upload Gate)")
                    .error("Chưa có tệp hình ảnh lá cây đậu nào được tải lên máy chủ.")
                    .suggestion("Vui lòng chọn hoặc kéo thả tệp hình ảnh lá đậu hợp lệ (.JPG, .PNG, .WEBP).")
                    .build();
        }

        // Kiểm tra phần mở rộng định dạng tệp ảnh
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        } else {
            fileExtension = ".jpg";
        }

        if (!fileExtension.matches("\\.(jpg|jpeg|png|webp|bmp)")) {
            return PredictionResultDTO.builder()
                    .success(false)
                    .errorTitle("Định dạng tệp không được hỗ trợ")
                    .errorLocation("Bộ lọc định dạng tệp máy chủ (File Format Validator)")
                    .error("Hệ thống chỉ hỗ trợ các định dạng hình ảnh JPG, JPEG, PNG, WEBP.")
                    .suggestion("Vui lòng chuyển đổi tệp ảnh của bạn sang định dạng JPG hoặc PNG trước khi gửi phân tích.")
                    .build();
        }

        try {
            // 1. Tạo thư mục uploads nếu chưa tồn tại
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Lưu file với tên ngẫu nhiên tránh trùng lặp
            String uniqueFilename = UUID.randomUUID() + fileExtension;
            Path destinationPath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            // 3. Chuẩn bị đường dẫn model và script
            File modelFile = new File(modelPath);
            if (!modelFile.exists()) {
                File targetModel = new File("target/classes/static/best.pt");
                if (targetModel.exists()) {
                    modelFile = targetModel;
                }
            }
            String absoluteModelPath = modelFile.getAbsolutePath();
            String absoluteImagePath = destinationPath.toFile().getAbsolutePath();
            String absoluteScriptPath = new File(scriptPath).getAbsolutePath();
            String pythonCmd = resolvePythonExecutable();

            // 4. Thực thi tiến trình Python để chạy suy luận mô hình YOLO best.pt
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCmd,
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
                log.error("AI execution error: exitCode={}, output={}", exitCode, jsonOutput);
                return PredictionResultDTO.builder()
                        .success(false)
                        .errorTitle("Không thể nhận diện bệnh trên ảnh")
                        .errorLocation("Tiến trình suy luận mô hình AI (ai-service/predict.py - best.pt)")
                        .error("Mô hình AI không thể trích xuất đặc trưng bệnh từ hình ảnh này (ảnh có thể bị mờ, lóa hoặc không chụp rõ lá).")
                        .suggestion("Vui lòng chụp lại ảnh lá đậu ở cự ly gần, rõ nét bề mặt vết bệnh và đảm bảo ánh sáng tốt.")
                        .build();
            }

            // Tìm chuỗi JSON trong output (phòng trường hợp ultralytics log ra stdout)
            int jsonStart = jsonOutput.indexOf("{");
            int jsonEnd = jsonOutput.lastIndexOf("}");
            if (jsonStart == -1 || jsonEnd == -1) {
                log.error("Invalid AI JSON output format: {}", jsonOutput);
                return PredictionResultDTO.builder()
                        .success(false)
                        .errorTitle("Lỗi định dạng dữ liệu từ mô hình AI")
                        .errorLocation("Bộ chuyển đổi kết quả suy luận AI (AI Result Parser)")
                        .error("Mô hình AI đã chạy nhưng không trả về cấu trúc dữ liệu JSON đúng chuẩn.")
                        .suggestion("Vui lòng thử gửi lại một ảnh lá đậu khác rõ ràng hơn.")
                        .build();
            }
            jsonOutput = jsonOutput.substring(jsonStart, jsonEnd + 1);

            // 6. Chuyển đổi JSON sang DTO
            JsonNode rootNode = objectMapper.readTree(jsonOutput);
            boolean success = rootNode.path("success").asBoolean(false);

            if (!success) {
                String errorMsg = rootNode.path("error").asText("Mô hình AI không thể nhận diện được bệnh trên ảnh.");
                return PredictionResultDTO.builder()
                        .success(false)
                        .errorTitle("Không thể nhận diện bệnh lá đậu")
                        .errorLocation("Mô hình AI nhận diện thực địa (best.pt)")
                        .error(errorMsg)
                        .suggestion("Hãy đảm bảo mẫu chụp là lá cây họ đậu và chụp lại ở cự ly gần hơn.")
                        .build();
            }

            Integer classId = rootNode.path("class_id").asInt();
            String className = rootNode.path("class_name").asText();
            String vietnameseName = rootNode.path("vietnamese_name").asText();
            String scientificName = rootNode.path("scientific_name").asText();
            String category = rootNode.path("category").asText("Nấm bệnh");
            Double confidence = rootNode.path("confidence").asDouble();
            String severity = rootNode.path("severity").asText();
            String severityCode = rootNode.path("severity_code").asText();
            String symptoms = rootNode.path("symptoms").asText();
            String spreadConditions = rootNode.path("spread_conditions").asText();
            String treatment = rootNode.path("treatment").asText();

            // Phác đồ điều trị đa giai đoạn (5 bước)
            JsonNode protoNode = rootNode.path("treatment_protocol");
            PredictionResultDTO.TreatmentProtocolDTO protocolDTO = null;
            if (!protoNode.isMissingNode() && !protoNode.isNull()) {
                protocolDTO = PredictionResultDTO.TreatmentProtocolDTO.builder()
                        .culturalMeasures(protoNode.path("cultural_measures").asText())
                        .biologicalTreatment(protoNode.path("biological_treatment").asText())
                        .chemicalTreatment(protoNode.path("chemical_treatment").asText())
                        .recoveryNutrition(protoNode.path("recovery_nutrition").asText())
                        .prevention(protoNode.path("prevention").asText())
                        .build();
            }

            // Top candidates
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

            // 7. Lưu lại lịch sử chẩn đoán vào cơ sở dữ liệu nếu có user đăng nhập
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
                    .category(category)
                    .confidence(confidence)
                    .severity(severity)
                    .severityCode(severityCode)
                    .symptoms(symptoms)
                    .spreadConditions(spreadConditions)
                    .treatment(treatment)
                    .treatmentProtocol(protocolDTO)
                    .imageUrl(publicImageUrl)
                    .topCandidates(candidates)
                    .build();

        } catch (Exception e) {
            log.error("Exception during AI prediction: ", e);
            return PredictionResultDTO.builder()
                    .success(false)
                    .errorTitle("Sự cố xử lý nội bộ máy chủ")
                    .errorLocation("Dịch vụ chẩn đoán bệnh cây đậu (AiPredictionService)")
                    .error("Hệ thống gặp sự cố trong quá trình lưu trữ hoặc phân tích hình ảnh.")
                    .suggestion("Vui lòng thử lại với một ảnh lá đậu khác hoặc thử lại sau giây lát.")
                    .build();
        }
    }

    /**
     * Lấy toàn bộ danh mục 11 loại sâu bệnh và phác đồ điều trị mẫu
     */
    public JsonNode getAllDiseases() {
        try {
            String pythonCmd = resolvePythonExecutable();
            String absoluteScriptPath = new File(scriptPath).getAbsolutePath();
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCmd,
                    absoluteScriptPath,
                    "--list-all"
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder outputBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line);
                }
            }

            process.waitFor();
            String jsonOutput = outputBuilder.toString().trim();
            int jsonStart = jsonOutput.indexOf("{");
            int jsonEnd = jsonOutput.lastIndexOf("}");
            if (jsonStart != -1 && jsonEnd != -1) {
                jsonOutput = jsonOutput.substring(jsonStart, jsonEnd + 1);
                return objectMapper.readTree(jsonOutput);
            }
        } catch (Exception e) {
            log.error("Failed to fetch all diseases: ", e);
        }
        return objectMapper.createObjectNode().put("success", false);
    }
}
