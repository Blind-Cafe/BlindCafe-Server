package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.NoticeLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface NoticeLogRepository extends MongoRepository<NoticeLog, Long> {
    Optional<NoticeLog> findByUserId(Long userId);
}
