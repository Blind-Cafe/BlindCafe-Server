package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.Reason;
import me.blindcafe.blindcafe.domain.type.ReasonType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReasonRepository extends JpaRepository<Reason, Long> {
    Optional<Reason> findByReasonTypeAndNum(ReasonType reasonType, Long num);
}
