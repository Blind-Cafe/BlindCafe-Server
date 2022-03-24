package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.RoomLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoomLogRepository extends MongoRepository<RoomLog, Long> {
    Optional<RoomLog> findFirstByMatchingIdAndUserIdOrderByAccessAtDesc(Long matchingId, Long userId);
}
