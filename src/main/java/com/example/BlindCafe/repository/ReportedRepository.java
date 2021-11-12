package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.Reported;
import com.example.BlindCafe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportedRepository extends JpaRepository<Reported, Long> {
    List<Reported> findByUser(User user);
}
