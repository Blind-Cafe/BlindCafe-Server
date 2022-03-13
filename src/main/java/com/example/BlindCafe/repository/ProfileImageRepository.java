package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.ProfileImage;
import com.example.BlindCafe.entity.type.status.CommonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    Optional<ProfileImage> findByUserIdAndPriorityAndStatus(Long userId, int priority, CommonStatus status);
}
