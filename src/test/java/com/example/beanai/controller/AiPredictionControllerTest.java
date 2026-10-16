package com.example.beanai.controller;

import com.example.beanai.dto.PredictionResultDTO;
import com.example.beanai.service.AiPredictionService;
import com.example.beanai.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JUnit 5 - Kiểm thử Controller AiPredictionController")
class AiPredictionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AiPredictionService aiPredictionService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AiPredictionController aiPredictionController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiPredictionController).build();
    }

    @Test
    @DisplayName("Test 1: GET /api/ai/diseases -> Trả về HTTP 200 và danh sách 11 loại sâu bệnh")
    void testGetAllDiseases() throws Exception {
        ObjectNode sampleNode = objectMapper.createObjectNode();
        sampleNode.put("success", true);
        ArrayNode diseasesArray = sampleNode.putArray("diseases");
        ObjectNode diseaseItem = diseasesArray.addObject();
        diseaseItem.put("class_id", 8);
        diseaseItem.put("vietnamese_name", "Bệnh gỉ sắt");

        when(aiPredictionService.getAllDiseases()).thenReturn(sampleNode);

        mockMvc.perform(get("/api/ai/diseases"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.diseases[0].class_id").value(8))
                .andExpect(jsonPath("$.diseases[0].vietnamese_name").value("Bệnh gỉ sắt"));
    }

    @Test
    @DisplayName("Test 2: POST /api/ai/predict với ảnh hợp lệ -> Trả về HTTP 200 và kết quả chẩn đoán thành công")
    void testPredict_Success() throws Exception {
        PredictionResultDTO successResult = PredictionResultDTO.builder()
                .success(true)
                .classId(8)
                .className("Rust")
                .vietnameseName("Bệnh gỉ sắt")
                .scientificName("Uromyces appendiculatus")
                .confidence(98.5)
                .severity("Cao")
                .severityCode("HIGH")
                .category("Nấm bệnh")
                .imageUrl("/uploads/sample.jpg")
                .build();

        when(aiPredictionService.predictDisease(any(), any())).thenReturn(successResult);

        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "leaf.jpg", "image/jpeg", "image content".getBytes());

        mockMvc.perform(multipart("/api/ai/predict").file(imageFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.classId").value(8))
                .andExpect(jsonPath("$.vietnameseName").value("Bệnh gỉ sắt"))
                .andExpect(jsonPath("$.confidence").value(98.5))
                .andExpect(jsonPath("$.imageUrl").value("/uploads/sample.jpg"));
    }

    @Test
    @DisplayName("Test 3: POST /api/ai/predict khi Service trả về lỗi định dạng -> Trả về thông báo lỗi có cấu trúc")
    void testPredict_Failure_StructuredError() throws Exception {
        PredictionResultDTO errorResult = PredictionResultDTO.builder()
                .success(false)
                .errorTitle("Định dạng tệp không được hỗ trợ")
                .errorLocation("Bộ lọc định dạng tệp máy chủ (File Format Validator)")
                .error("Hệ thống chỉ hỗ trợ các định dạng hình ảnh JPG, JPEG, PNG, WEBP.")
                .suggestion("Vui lòng chuyển đổi tệp ảnh của bạn sang định dạng JPG hoặc PNG.")
                .build();

        when(aiPredictionService.predictDisease(any(), any())).thenReturn(errorResult);

        MockMultipartFile badFile = new MockMultipartFile(
                "image", "sample.txt", "text/plain", "plain text".getBytes());

        mockMvc.perform(multipart("/api/ai/predict").file(badFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorTitle").value("Định dạng tệp không được hỗ trợ"))
                .andExpect(jsonPath("$.errorLocation").value("Bộ lọc định dạng tệp máy chủ (File Format Validator)"))
                .andExpect(jsonPath("$.suggestion").exists());
    }
}
