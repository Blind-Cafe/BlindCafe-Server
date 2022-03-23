package com.example.BlindCafe.repository;

import com.example.BlindCafe.domain.topic.Audio;
import com.example.BlindCafe.domain.topic.Image;
import com.example.BlindCafe.domain.topic.Subject;
import com.example.BlindCafe.domain.topic.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    @Query("SELECT s FROM Subject s WHERE s.interestId=:interestId")
    List<Topic> findSubjectByInterestId(@Param("interestId") Long interestId);

    @Query("SELECT s FROM Subject s WHERE s.interestId NOT IN ?1")
    List<Topic> findSubjectByInterestIdNotIN(List<Long> ids);

    @Query(value = "SELECT * FROM topic WHERE dtype = 'I'", nativeQuery = true)
    List<Topic> findImages();

    @Query(value = "SELECT * FROM topic WHERE dtype = 'A'", nativeQuery = true)
    List<Topic> findAudios();

    Optional<Subject> findSubjectById(Long id);
    Optional<Audio> findAudioById(Long id);
    Optional<Image> findImageById(Long id);
}
