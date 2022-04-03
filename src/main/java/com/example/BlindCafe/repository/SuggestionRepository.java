package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    @Query(value = "SELECT COUNT(*) FROM suggestion WHERE `check` = false", nativeQuery = true)
    Long countUncheckedSuggestion();
}
