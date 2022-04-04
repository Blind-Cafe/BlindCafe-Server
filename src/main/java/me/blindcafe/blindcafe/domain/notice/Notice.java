package me.blindcafe.blindcafe.domain.notice;


import me.blindcafe.blindcafe.domain.BaseTimeEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
public abstract class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;
}
