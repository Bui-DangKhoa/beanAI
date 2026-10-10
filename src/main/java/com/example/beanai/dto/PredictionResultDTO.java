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
    private Integer classId;
    private String className;
    private String vietnameseName;
    private String scientificName;
    private Double confidence;
    private String severity;
    private String severityCode;
    private String symptoms;
    private String treatment;
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
}
