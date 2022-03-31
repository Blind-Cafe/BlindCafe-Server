package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.Matching;
import com.example.BlindCafe.domain.type.status.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {
    @Query(value = "SELECT * FROM matching m WHERE m.matching_id = ?1 AND m.status = 'MATCHING'", nativeQuery = true)
    Optional<Matching> findValidMatchingById(Long matchingId);
    @Query("SELECT m FROM Matching m WHERE m.status = ?1 AND m.isActive = ?2")
    List<Matching> findByStatusAndActive(MatchingStatus status, boolean isActive);
}
