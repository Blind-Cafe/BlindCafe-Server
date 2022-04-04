package me.blindcafe.blindcafe.domain;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "user_interest")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInterest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_interest_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "interest_id")
    private Interest interest;

    private boolean active;

    public void setUser(User user) {
        this.user = user;
        user.getInterests().add(this);
    }

    public static UserInterest create(User user, Interest interest) {
        UserInterest userInterest = new UserInterest();
        userInterest.setUser(user);
        userInterest.setInterest(interest);
        userInterest.setActive(true);
        return userInterest;
    }

    public void remove() {
        this.setActive(false);
        this.getUser().getAvatars().remove(this);
    }
}
