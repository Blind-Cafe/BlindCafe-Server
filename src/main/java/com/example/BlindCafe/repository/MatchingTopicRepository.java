package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.Matching;
import com.example.BlindCafe.domain.MatchingTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchingTopicRepository extends JpaRepository<MatchingTopic, Long> {
    List<MatchingTopic> findAllByMatching(Matching matching);
}
