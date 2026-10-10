package com.example.beanai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity lưu vết lịch sử các lần chẩn đoán sâu bệnh của người dùng
 */
@Entity
@Table(name = "diagnosis_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "class_name", length = 100)
    private String className;

    @Column(name = "vietnamese_name", nullable = false, length = 150)
    private String vietnameseName;

    @Column(name = "scientific_name", length = 150)
    private String scientificName;

    @Column(nullable = false)
    private Double confidence;

    @Column(length = 50)
    private String severity;

    @Column(length = 50)
    private String severityCode;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
