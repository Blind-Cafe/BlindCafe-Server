package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.Ticket;
import com.example.BlindCafe.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByUser(User user);
}
