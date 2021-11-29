package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.Matching;
import com.example.BlindCafe.entity.RoomLog;
import com.example.BlindCafe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomLogRepository extends JpaRepository<RoomLog, Long> {
    List<RoomLog> findByUserAndMatching(User user, Matching matching);
}
