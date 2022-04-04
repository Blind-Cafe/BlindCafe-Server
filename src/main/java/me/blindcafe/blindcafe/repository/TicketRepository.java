package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.Ticket;
import me.blindcafe.blindcafe.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByUser(User user);
}
