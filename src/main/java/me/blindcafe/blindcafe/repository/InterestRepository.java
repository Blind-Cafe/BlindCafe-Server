package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {
    List<Interest> findByIdIn(List<Long> ids);
}
