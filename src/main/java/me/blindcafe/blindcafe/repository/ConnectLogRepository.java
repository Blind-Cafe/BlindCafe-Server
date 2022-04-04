package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.ConnectLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConnectLogRepository extends MongoRepository<ConnectLog, String> {
    List<ConnectLog> findByAccessDay(String accessDay);
}
