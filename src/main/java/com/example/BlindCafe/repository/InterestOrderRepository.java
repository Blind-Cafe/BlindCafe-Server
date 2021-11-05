package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.InterestOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestOrderRepository extends JpaRepository<InterestOrder, Long> {
}