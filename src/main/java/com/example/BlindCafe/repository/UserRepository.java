package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.type.status.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialId(String socialId);

    Optional<User> findByPhone(String phone);

    List<User> findByStatus(UserStatus status);

    @Query(value = "SELECT u FROM user u WHERE u.status = 'NORMAL' AND u.platform = ?1", nativeQuery = true)
    List<User> findNormalUsersByPlatform(String platform);
}
