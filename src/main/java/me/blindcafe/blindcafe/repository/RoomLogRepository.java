package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.RoomLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoomLogRepository extends MongoRepository<RoomLog, String> {
    RoomLog findRoomLogByMatchingId(String matchingId);
}
