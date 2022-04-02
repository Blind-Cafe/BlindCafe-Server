package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, Long> {
    Message findFirstByMatchingIdOrderByCreatedAtDesc(String matchingId);
    Page<Message> findByMatchingId(String matchingId, Pageable pageable);
}
