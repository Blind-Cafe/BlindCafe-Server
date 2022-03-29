package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    @Query(value = "SELECT s FROM notification_setting s WHERE s.user_id = ?1", nativeQuery = true)
    Optional<NotificationSetting> findByUserId(Long userId);
}
