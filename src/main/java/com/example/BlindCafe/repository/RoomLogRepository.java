package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.Matching;
import com.example.BlindCafe.domain.RoomLog;
import com.example.BlindCafe.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomLogRepository extends JpaRepository<RoomLog, Long> {
    List<RoomLog> findByUserAndMatching(User user, Matching matching);
}
