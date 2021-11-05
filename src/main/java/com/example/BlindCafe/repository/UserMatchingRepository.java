package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.UserMatching;
import com.example.BlindCafe.type.status.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMatchingRepository extends JpaRepository<UserMatching, Long> {
    List<UserMatching> findByStatus(MatchingStatus status);
}
