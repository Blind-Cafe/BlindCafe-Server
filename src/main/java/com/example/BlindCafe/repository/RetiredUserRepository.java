package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.RetiredUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetiredUserRepository extends JpaRepository<RetiredUser, Long> {
}
