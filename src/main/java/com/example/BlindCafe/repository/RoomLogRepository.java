package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.RoomLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoomLogRepository extends MongoRepository<RoomLog, String> {
    RoomLog findRoomLogByMatchingId(String matchingId);
}
