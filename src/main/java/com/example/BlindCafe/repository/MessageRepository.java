package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
