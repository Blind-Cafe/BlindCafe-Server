package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.DailyConnect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DailyConnectRepository extends JpaRepository<DailyConnect, String> {
    @Query(value = "SELECT * FROM daily_connect WHERE daily_connect_id >= ?1 AND daily_connect_id <= ?2 ORDER BY daily_connect_id", nativeQuery = true)
    List<DailyConnect> getWeeklyState(String begin, String end);
}
