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
    
    // 매칭 요청 중인지 조회
    @Query("SELECT um FROM UserMatching um WHERE um.status = 'WAIT' AND um.user = ?1")
    Optional<UserMatching> findMatchingRequestByUserId(Long userId);
    
    // 매칭 풀에서 매칭 전적이 없는 사용자 조회
    @Query("SELECT um FROM UserMatching um WHERE um.status = 'WAIT' AND um.user NOT IN ?1")
    List<UserMatching> findAbleMatchingRequests(List<Long> ids);
    
    // 사용자의 유효한 매칭 조회
    @Query("SELECT um FROM UserMatching um WHERE um.status = 'MATCHING' AND um.user = ?1")
    List<UserMatching> findMatchingByUserId(Long userId);
}
