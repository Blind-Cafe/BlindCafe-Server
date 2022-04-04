package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.RetiredUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetiredUserRepository extends JpaRepository<RetiredUser, Long> {
}
