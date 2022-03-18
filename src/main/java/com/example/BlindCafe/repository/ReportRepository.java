package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.Report;
import com.example.BlindCafe.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReported(User user);
    List<Report> findByReporter(User user);
}
