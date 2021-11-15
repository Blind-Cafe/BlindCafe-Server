package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.Report;
import com.example.BlindCafe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReported(User user);
}
