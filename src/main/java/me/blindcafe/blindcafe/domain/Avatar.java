package me.blindcafe.blindcafe.domain;

import me.blindcafe.blindcafe.domain.type.status.CommonStatus;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "avatar")
public class Avatar extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "avatar_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String src;

    private int sequence;

    @Enumerated(STRING)
    private CommonStatus status;

    public static Avatar create(
            User user,
            String src,
            int sequence
    ) {
        Avatar avatar = new Avatar();
        avatar.setUser(user);
        avatar.setSrc(src);
        avatar.setStatus(CommonStatus.NORMAL);
        avatar.setSequence(sequence);
        return avatar;
    }

    public void remove() {
        this.getUser().getAvatars().remove(this);
        this.setStatus(CommonStatus.DELETED);
    }
}
