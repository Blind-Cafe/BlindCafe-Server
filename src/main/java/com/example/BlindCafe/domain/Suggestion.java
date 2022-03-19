package com.example.BlindCafe.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "suggestion")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggestion_id")
    private Long id;

    private Long userId;

    private String nickname;

    private String phone;

    private String content;

    private String image;

    @CreatedDate
    private LocalDateTime createdAt;

    public static Suggestion create(User user, String content) {
        Suggestion suggestion = new Suggestion();
        suggestion.setUserId(user.getId());
        suggestion.setNickname(user.getNickname());
        suggestion.setPhone(user.getPhone());
        suggestion.setContent(content);
        return suggestion;
    }

    public void updateImage(String image) {
        this.setImage(image);
    }
}
