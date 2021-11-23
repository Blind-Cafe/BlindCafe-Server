package com.example.BlindCafe.entity;

import com.example.BlindCafe.type.Social;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RetiredUser extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "retired_user_id")
    private Long id;

    @Column(length = 10)
    private String nickname;

    @Column(name="social_id")
    private String socialId;

    @Enumerated(STRING)
    @Column(length = 10, nullable = false)
    private Social socialType;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "reason_id")
    private Reason reason;
}
