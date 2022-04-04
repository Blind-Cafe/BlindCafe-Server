package me.blindcafe.blindcafe.repository;

import me.blindcafe.blindcafe.domain.notice.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findBy(Pageable pageable);
    Optional<Notice> findFirstByOrderByCreatedAtDesc();
}
