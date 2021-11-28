package com.example.BlindCafe.entity;

import com.example.BlindCafe.type.MessageType;
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
public class Message extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String contents;

    @Enumerated(STRING)
    private MessageType type;

    public void setMatching(Matching matching) {
        this.matching = matching;
        matching.getMessages().add(this);
    }
}
