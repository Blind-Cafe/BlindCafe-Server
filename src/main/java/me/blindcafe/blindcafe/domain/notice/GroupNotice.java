package me.blindcafe.blindcafe.domain.notice;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("G")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupNotice extends Notice {

    public static GroupNotice create(String title, String content) {
        GroupNotice notice = new GroupNotice();
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }
}
