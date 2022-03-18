package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    void deleteAllByUser(User user);
}
