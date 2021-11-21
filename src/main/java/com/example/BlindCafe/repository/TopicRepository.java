package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.topic.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Todo
 * subject 지금 관심사 별로 가져오고 있는데 동적 쿼리로 수정하기
 */

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByInterestId(Long interestId);

    @Query(value = "SELECT * FROM topic WHERE dtype = 'I' ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<Topic> findImages();

    @Query(value = "SELECT * FROM topic WHERE dtype = 'A' ORDER BY RAND() LIMIT 4", nativeQuery = true)
    List<Topic> findAudios();
}
