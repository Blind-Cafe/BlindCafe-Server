package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.CustomReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomReasonRepository extends JpaRepository<CustomReason, Long> {
}
