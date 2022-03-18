package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.Reason;
import com.example.BlindCafe.domain.type.ReasonType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReasonRepository extends JpaRepository<Reason, Long> {
    Optional<Reason> findByReasonTypeAndNum(ReasonType reasonType, Long num);
}
