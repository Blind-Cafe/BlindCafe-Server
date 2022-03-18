package com.example.BlindCafe.domain;

import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.exception.CodeAndMessage;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "ticket")
@Getter
@Setter
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue
    @Column(name = "ticket_id")
    private Long id;

    private Long userId;

    private int count;

    public static Ticket create(Long userId) {
        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setCount(0);
        ticket.init();
        return ticket;
    }

    public void init() {
        this.setCount(3);
    }

    // TODO 에러 타입 정하기
    public void match() {
        if (this.count == 0)
            throw new BlindCafeException(CodeAndMessage.INTERNAL_SERVER_ERROR);
        this.count -= 1;
    }
}
