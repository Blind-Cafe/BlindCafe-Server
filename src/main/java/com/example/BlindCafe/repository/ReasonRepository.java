package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.Reason;
import com.example.BlindCafe.entity.type.ReasonType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReasonRepository extends JpaRepository<Reason, Long> {
    Optional<Reason> findByReasonTypeAndNum(ReasonType reasonType, Long num);
}
