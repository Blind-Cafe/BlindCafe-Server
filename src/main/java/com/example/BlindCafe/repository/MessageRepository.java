package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.Matching;
import com.example.BlindCafe.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByMatching(Matching matching);
}
