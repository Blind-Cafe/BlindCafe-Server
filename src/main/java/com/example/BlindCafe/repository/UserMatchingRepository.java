package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.UserMatching;
import com.example.BlindCafe.domain.type.status.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMatchingRepository extends JpaRepository<UserMatching, Long> {
    
    // 매칭 요청 중인지 조회
    @Query(value = "SELECT * FROM user_matching um WHERE um.user_id = ?1  AND um.status = 'WAIT'", nativeQuery = true)
    Optional<UserMatching> findMatchingRequestByUserId(Long userId);
    
    // 매칭 풀에서 매칭 전적이 없는 사용자 조회
    @Query(value = "SELECT * FROM user_matching um WHERE um.status = 'WAIT' AND um.user_id NOT IN ?1", nativeQuery = true)
    List<UserMatching> findAbleMatchingRequests(List<Long> ids);
    
    // 사용자의 유효한 매칭 조회
    @Query(value = "SELECT * FROM user_matching um WHERE um.status = 'MATCHING' AND um.user_id = ?1", nativeQuery = true)
    List<UserMatching> findMatchingByUserId(Long userId);

    // 특정 시간보다 이전에 발생한 매칭 요청
    @Query(value = "SELECT * FROM user_matching um WHERE um.status = 'WAIT' AND um.created_at < ?1", nativeQuery = true)
    List<UserMatching> findAgingMatchingRequests(LocalDateTime time);

}
