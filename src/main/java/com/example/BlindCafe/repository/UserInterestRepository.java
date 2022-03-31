package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUserAndActive(User user, boolean isActive);
}
