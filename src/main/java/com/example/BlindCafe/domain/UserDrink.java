package com.example.BlindCafe.domain;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "user_drink")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDrink extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "user_drink_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "drink_id")
    private Drink drink;

    public static UserDrink create(User user, Drink drink) {
        UserDrink userDrink = new UserDrink();
        userDrink.setUser(user);
        userDrink.setDrink(drink);
        return userDrink;
    }
}
