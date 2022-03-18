package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.Avatar;
import com.example.BlindCafe.domain.type.status.CommonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileImageRepository extends JpaRepository<Avatar, Long> {
    Optional<Avatar> findByUserIdAndPriorityAndStatus(Long userId, int priority, CommonStatus status);
}
