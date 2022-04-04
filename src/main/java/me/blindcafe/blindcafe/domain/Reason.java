package me.blindcafe.blindcafe.domain;

import me.blindcafe.blindcafe.domain.type.ReasonType;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reason_id")
    private Long id;

    private Long num;

    private String text;

    @Enumerated(STRING)
    private ReasonType reasonType;
}
