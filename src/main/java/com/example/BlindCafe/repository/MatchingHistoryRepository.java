package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.MatchingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingHistoryRepository extends JpaRepository<MatchingHistory, Long> {
    MatchingHistory findByUserId(Long userId);
}
