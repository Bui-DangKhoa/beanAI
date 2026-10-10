package com.example.beanai.repository;

import com.example.beanai.entity.DiagnosisHistory;
import com.example.beanai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác với bảng diagnosis_history
 */
@Repository
public interface DiagnosisHistoryRepository extends JpaRepository<DiagnosisHistory, Long> {
    List<DiagnosisHistory> findByUserOrderByCreatedAtDesc(User user);
    long countByUser(User user);
}
