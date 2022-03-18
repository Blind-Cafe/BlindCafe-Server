package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.NoticeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeLogRepository extends JpaRepository<NoticeLog, Long> {
    Optional<NoticeLog> findByUserId(Long userId);
}
