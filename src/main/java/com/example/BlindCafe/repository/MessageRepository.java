package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.Matching;
import com.example.BlindCafe.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByMatching(Matching matching);

    Optional<Message> findByMatching(Matching matching);
}
