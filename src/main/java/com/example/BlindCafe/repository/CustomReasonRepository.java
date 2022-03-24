package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.CustomReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomReasonRepository extends JpaRepository<CustomReason, Long> {
}
