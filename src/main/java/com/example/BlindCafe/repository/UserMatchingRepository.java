package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.UserMatching;
import com.example.BlindCafe.domain.type.status.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMatchingRepository extends JpaRepository<UserMatching, Long> {
    List<UserMatching> findByStatus(MatchingStatus status);
    @Query("SELECT um FROM UserMatching um WHERE um.user = ?1 AND um.status = 'WAIT'")
    Optional<UserMatching> findMatchingRequestByUserId(Long userId);
}
