package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.MatchingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingHistoryRepository extends JpaRepository<MatchingHistory, Long> {
    MatchingHistory findByUserId(Long userId);
}
