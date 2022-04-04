package me.blindcafe.blindcafe.domain;

import me.blindcafe.blindcafe.exception.BlindCafeException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

import static me.blindcafe.blindcafe.exception.CodeAndMessage.LACK_OF_TICKET;

@Entity
@Table(name = "ticket")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "ticket", fetch = FetchType.LAZY)
    private User user;

    private int count;

    public static Ticket create() {
        Ticket ticket = new Ticket();
        ticket.setCount(0);
        ticket.init();
        return ticket;
    }

    public void init() {
        this.setCount(3);
    }

    public void consume() {
        if (this.count == 0)
            throw new BlindCafeException(LACK_OF_TICKET);
        this.count -= 1;
    }

    public void restore() {
        if (this.count < 3)
            this.count += 1;
    }
}
