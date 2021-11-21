package com.example.BlindCafe.repository;

import com.example.BlindCafe.entity.topic.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Todo
 * subject 지금 관심사 별로 가져오고 있는데 동적 쿼리로 수정하기
 */
public interface TopicRepository extends JpaRepository<Topic, Long> {
    @Query("select s from Subject s where s.interestId=:interestId")
    List<Topic> findByInterestId(@Param("interestId") Long interestId);

    @Query(value = "SELECT * FROM topic WHERE dtype = 'I'", nativeQuery = true)
    List<Topic> findImages();

    @Query(value = "SELECT * FROM topic WHERE dtype = 'A'", nativeQuery = true)
    List<Topic> findAudios();
}
