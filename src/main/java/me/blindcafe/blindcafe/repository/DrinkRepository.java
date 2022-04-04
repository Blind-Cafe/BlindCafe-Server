package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.Drink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrinkRepository extends JpaRepository<Drink, Long> {
}
