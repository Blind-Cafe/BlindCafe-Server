package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.UserDrink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDrinkRepository extends JpaRepository<UserDrink, Long> {
}
