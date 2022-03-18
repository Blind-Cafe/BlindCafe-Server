package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.type.status.CommonStatus;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "profile_image")
public class Avatar extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "profile_image_id")
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
