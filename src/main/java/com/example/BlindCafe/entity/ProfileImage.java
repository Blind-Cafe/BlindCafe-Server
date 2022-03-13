package com.example.BlindCafe.entity;

import com.example.BlindCafe.entity.type.status.CommonStatus;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileImage extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "profile_image_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String src;

    private int priority;

    @Enumerated(STRING)
    private CommonStatus status;
}
