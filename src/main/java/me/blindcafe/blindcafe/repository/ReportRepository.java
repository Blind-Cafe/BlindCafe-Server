package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.Report;
import me.blindcafe.blindcafe.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByReporter(User user, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM report WHERE `check` = false", nativeQuery = true)
    Long countUncheckedReport();
}
