package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.notice.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query(value = "SELECT n FROM Notice n WHERE n.dtype = 'G' ORDER BY n.createdAt DESC LIMIT ?1 OFFSET ?2", nativeQuery = true)
    List<Notice> findGroupNoticeByPage(int size, int offset);

    @Query(value = "SELECT n FROM Notice n WHERE n.dtype = 'G' AND n.id < ?2 ORDER BY n.createdAt DESC LIMIT ?1", nativeQuery = true)
    List<Notice> findGroupNoticeByOffset(int size, Long offset);

    Optional<Notice> findFirstByOrderByCreatedAtDesc();
}
