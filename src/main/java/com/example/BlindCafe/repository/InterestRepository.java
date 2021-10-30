package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByIdAndParentId(Long id, Long parentId);
}
