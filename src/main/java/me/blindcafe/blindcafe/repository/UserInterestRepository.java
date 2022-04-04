package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.User;
import me.blindcafe.blindcafe.domain.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUserAndActive(User user, boolean isActive);
}
