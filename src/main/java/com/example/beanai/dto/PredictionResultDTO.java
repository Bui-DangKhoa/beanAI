package com.example.beanai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO chứa kết quả phân tích sâu bệnh trả về từ mô hình AI (best.pt)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResultDTO {
    private boolean success;
    private String error;
    private String errorTitle;       // Tên lỗi (Lỗi gì)
    private String errorLocation;    // Vị trí phát sinh (Ở đâu)
    private String suggestion;       // Hướng xử lý đề xuất cho người dùng
    private Integer classId;
    private String className;
    private String vietnameseName;
    private String scientificName;
    private Double confidence;
    private String severity;
    private String severityCode;
    private String category;
    private String spreadConditions;
    private String symptoms;
    private String treatment;
    private TreatmentProtocolDTO treatmentProtocol;
    private String imageUrl;
    private List<CandidateDTO> topCandidates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CandidateDTO {
        private Integer classId;
        private String className;
        private String vietnameseName;
        private Double confidence;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TreatmentProtocolDTO {
        private String culturalMeasures;
        private String biologicalTreatment;
        private String chemicalTreatment;
        private String recoveryNutrition;
        private String prevention;
    }
}
