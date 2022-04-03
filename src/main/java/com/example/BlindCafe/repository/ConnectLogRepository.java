package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.ConnectLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConnectLogRepository extends MongoRepository<ConnectLog, String> {
    List<ConnectLog> findByAccessDay(String accessDay);
}
