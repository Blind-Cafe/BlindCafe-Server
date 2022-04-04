package me.blindcafe.blindcafe.domain.notice;

import me.blindcafe.blindcafe.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@DiscriminatorValue("P")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalNotice extends Notice {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static PersonalNotice create(String title, String content, User user) {
        PersonalNotice notice = new PersonalNotice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setUser(user);
        return notice;
    }
}
