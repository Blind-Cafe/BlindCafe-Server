package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialId(String socialId);
    Optional<User> findByPhone(String phone);
    @Query(value = "SELECT u FROM User u WHERE u.status = 'NORMAL' AND u.platform = ?1", nativeQuery = true)
    List<User> findNormalUsersByPlatform(String platform);
}
