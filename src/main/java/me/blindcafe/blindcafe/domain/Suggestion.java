package me.blindcafe.blindcafe.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "suggestion")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Suggestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggestion_id")
    private Long id;

    private Long userId;

    private String nickname;

    private String phone;

    private String content;

    private String image;

    private boolean check;

    public static Suggestion create(User user, String content) {
        Suggestion suggestion = new Suggestion();
        suggestion.setUserId(user.getId());
        suggestion.setNickname(user.getNickname());
        suggestion.setPhone(user.getPhone());
        suggestion.setContent(content);
        suggestion.setCheck(false);
        return suggestion;
    }

    public void updateImage(String image) {
        this.setImage(image);
    }
}
