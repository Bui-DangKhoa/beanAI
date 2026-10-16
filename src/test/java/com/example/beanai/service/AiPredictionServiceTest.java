package com.example.beanai.service;

import com.example.beanai.dto.PredictionResultDTO;
import com.example.beanai.repository.DiagnosisHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JUnit 5 - Kiểm thử Service AiPredictionService")
class AiPredictionServiceTest {

    @Mock
    private DiagnosisHistoryRepository diagnosisHistoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AiPredictionService aiPredictionService;

    @BeforeEach
    void setUp() {
        aiPredictionService = new AiPredictionService(diagnosisHistoryRepository, objectMapper);
        ReflectionTestUtils.setField(aiPredictionService, "configuredPythonPath", "python");
        ReflectionTestUtils.setField(aiPredictionService, "uploadDir", "uploads");
        ReflectionTestUtils.setField(aiPredictionService, "scriptPath", "ai-service/predict.py");
        ReflectionTestUtils.setField(aiPredictionService, "modelPath", "src/main/resources/static/best.pt");
    }

    @Test
    @DisplayName("Test 1: Tệp null -> Trả về lỗi tệp rỗng rõ ràng 'Lỗi gì' và 'Ở đâu'")
    void testPredictDisease_NullFile() {
        PredictionResultDTO result = aiPredictionService.predictDisease(null, null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Tệp hình ảnh không hợp lệ", result.getErrorTitle());
        assertEquals("Cổng tiếp nhận tệp của máy chủ (Web Server Upload Gate)", result.getErrorLocation());
        assertNotNull(result.getSuggestion());
    }

    @Test
    @DisplayName("Test 2: Tệp rỗng 0 bytes -> Trả về lỗi tệp không hợp lệ")
    void testPredictDisease_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);
        PredictionResultDTO result = aiPredictionService.predictDisease(emptyFile, null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Tệp hình ảnh không hợp lệ", result.getErrorTitle());
    }

    @Test
    @DisplayName("Test 3: Tải lên tệp không phải định dạng ảnh (.pdf) -> Bị chặn và nêu rõ lý do")
    void testPredictDisease_UnsupportedExtension_Pdf() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "image", "document.pdf", "application/pdf", "Fake PDF file content".getBytes());
        PredictionResultDTO result = aiPredictionService.predictDisease(pdfFile, null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Định dạng tệp không được hỗ trợ", result.getErrorTitle());
        assertEquals("Bộ lọc định dạng tệp máy chủ (File Format Validator)", result.getErrorLocation());
        assertTrue(result.getError().contains("JPG"));
        assertNotNull(result.getSuggestion());
    }

    @Test
    @DisplayName("Test 4: Tải lên tệp thực thi độc hại (.exe) -> Bị chặn an toàn")
    void testPredictDisease_UnsupportedExtension_Exe() {
        MockMultipartFile exeFile = new MockMultipartFile(
                "image", "malicious.exe", "application/octet-stream", "Fake binary content".getBytes());
        PredictionResultDTO result = aiPredictionService.predictDisease(exeFile, null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Định dạng tệp không được hỗ trợ", result.getErrorTitle());
    }

    @Test
    @DisplayName("Test 5: Tự động phát hiện trình thông dịch Python hợp lệ")
    void testResolvePythonExecutable() {
        String pythonCmd = aiPredictionService.resolvePythonExecutable();
        assertNotNull(pythonCmd, "Python executable không được null");
        assertFalse(pythonCmd.trim().isEmpty(), "Python executable không được để trống");
    }

    @Test
    @DisplayName("Test 6: Tra cứu từ điển 11 loại bệnh học và phác đồ điều trị đầy đủ")
    void testGetAllDiseases() {
        JsonNode root = aiPredictionService.getAllDiseases();

        assertNotNull(root, "Kết quả từ điển bệnh không được null");
        assertTrue(root.path("success").asBoolean(), "Trạng thái success phải là true");
        JsonNode diseases = root.path("diseases");
        assertTrue(diseases.isArray(), "Danh sách diseases phải là mảng JSON");
        assertEquals(11, diseases.size(), "Phải có đúng 11 lớp bệnh của mô hình best.pt");

        // Kiểm tra cấu trúc phần tử đầu tiên
        JsonNode first = diseases.get(0);
        assertTrue(first.has("class_id"));
        assertTrue(first.has("vietnamese_name"));
        assertTrue(first.has("symptoms"));
        assertTrue(first.has("protocol"));
    }

    @Test
    @DisplayName("Test 7: Phân tích ảnh lá thực tế -> Thành công, trả về đúng bệnh và phác đồ")
    void testPredictDisease_ValidLeafImage() throws Exception {
        File leafImage = new File("src/main/resources/static/images/diseases/rust.jpg");
        if (leafImage.exists()) {
            byte[] bytes = Files.readAllBytes(leafImage.toPath());
            MockMultipartFile validFile = new MockMultipartFile("image", "rust.jpg", "image/jpeg", bytes);

            PredictionResultDTO result = aiPredictionService.predictDisease(validFile, null);

            assertNotNull(result);
            assertTrue(result.isSuccess(), "Phân tích ảnh lá thực tế phải thành công");
            assertNotNull(result.getVietnameseName(), "Tên tiếng Việt của bệnh không được null");
            assertNotNull(result.getScientificName(), "Tên khoa học không được null");
            assertNotNull(result.getTreatmentProtocol(), "Phác đồ điều trị 5 bước phải có");
            assertNotNull(result.getImageUrl(), "URL ảnh kết quả phải có");
            assertNull(result.getError(), "Khi thành công thì trường error phải là null");
        }
    }
}
