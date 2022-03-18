package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.notice.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findBy(Pageable pageable);

    @Query(value="SELECT n FROM Notice n WHERE n.dtype = 'G' ORDER BY n.id LIMIT ?1 OFFSET ?2", nativeQuery = true)
    List<Notice> findGroupNoticeByPage(int limit, Long offset);

    Optional<Notice> findTop1ByOrderByCreatedAtDesc();
}
