package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.Matching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {
    @Query("SELECT m FROM Matching m WHERE m.id = ?1 AND m.status = 'MATCHING'")
    Optional<Matching> findValidMatchingById(Long matchingId);
}
