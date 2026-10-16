package com.example.beanai.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JUnit 5 - Kiểm thử DTO PredictionResultDTO")
class PredictionResultDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Test 1: Tạo đối tượng thành công và kiểm tra đầy đủ các trường phác đồ điều trị 5 bước")
    void testSuccessDTOBuilder() {
        PredictionResultDTO.TreatmentProtocolDTO protocol = PredictionResultDTO.TreatmentProtocolDTO.builder()
                .culturalMeasures("Thu gom lá bệnh")
                .biologicalTreatment("Phun Trichoderma")
                .chemicalTreatment("Phun Hexaconazole")
                .recoveryNutrition("Bón phân hữu cơ vi sinh")
                .prevention("Luân canh cây trồng")
                .build();

        PredictionResultDTO dto = PredictionResultDTO.builder()
                .success(true)
                .classId(8)
                .className("Rust")
                .vietnameseName("Bệnh gỉ sắt")
                .scientificName("Uromyces appendiculatus")
                .confidence(98.5)
                .severity("Cao")
                .severityCode("HIGH")
                .category("Nấm bệnh")
                .treatmentProtocol(protocol)
                .topCandidates(Collections.singletonList(
                        PredictionResultDTO.CandidateDTO.builder()
                                .classId(8)
                                .className("Rust")
                                .vietnameseName("Bệnh gỉ sắt")
                                .confidence(98.5)
                                .build()
                ))
                .build();

        assertTrue(dto.isSuccess());
        assertEquals("Bệnh gỉ sắt", dto.getVietnameseName());
        assertEquals(98.5, dto.getConfidence());
        assertEquals("HIGH", dto.getSeverityCode());
        assertNotNull(dto.getTreatmentProtocol());
        assertEquals("Phun Trichoderma", dto.getTreatmentProtocol().getBiologicalTreatment());
        assertNull(dto.getError());
        assertNull(dto.getErrorTitle());
    }

    @Test
    @DisplayName("Test 2: Tạo đối tượng lỗi và kiểm tra các trường Lỗi gì - Ở đâu - Hướng xử lý")
    void testErrorDTOBuilder() {
        PredictionResultDTO errorDto = PredictionResultDTO.builder()
                .success(false)
                .errorTitle("Ảnh vượt quá dung lượng cho phép")
                .errorLocation("Cổng tiếp nhận tệp của máy chủ (Web Server Upload Gate)")
                .error("Kích thước tệp hình ảnh vượt quá giới hạn 25MB.")
                .suggestion("Vui lòng chọn ảnh có dung lượng dưới 25MB.")
                .build();

        assertFalse(errorDto.isSuccess());
        assertEquals("Ảnh vượt quá dung lượng cho phép", errorDto.getErrorTitle());
        assertEquals("Cổng tiếp nhận tệp của máy chủ (Web Server Upload Gate)", errorDto.getErrorLocation());
        assertTrue(errorDto.getError().contains("25MB"));
        assertNotNull(errorDto.getSuggestion());
    }

    @Test
    @DisplayName("Test 3: Chuyển đổi JSON qua lại bằng Jackson ObjectMapper không bị lỗi")
    void testJsonSerialization() throws Exception {
        PredictionResultDTO original = PredictionResultDTO.builder()
                .success(false)
                .errorTitle("Định dạng tệp không được hỗ trợ")
                .errorLocation("Bộ lọc định dạng tệp máy chủ")
                .error("Chỉ chấp nhận ảnh JPG/PNG/WEBP.")
                .suggestion("Đổi đuôi ảnh và thử lại.")
                .build();

        String json = objectMapper.writeValueAsString(original);
        assertNotNull(json);
        assertTrue(json.contains("errorTitle"));
        assertTrue(json.contains("errorLocation"));
        assertTrue(json.contains("suggestion"));

        PredictionResultDTO deserialized = objectMapper.readValue(json, PredictionResultDTO.class);
        assertEquals(original.getErrorTitle(), deserialized.getErrorTitle());
        assertEquals(original.getErrorLocation(), deserialized.getErrorLocation());
        assertEquals(original.getError(), deserialized.getError());
    }
}
