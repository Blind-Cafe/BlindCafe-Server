package me.blindcafe.blindcafe.dto.response;

import me.blindcafe.blindcafe.domain.*;
import me.blindcafe.blindcafe.domain.type.Mbti;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {
    private List<String> avatars;
    private String nickname;
    private int age;
    private String address;
    private Mbti mbti;
    private String voice;
    private List<Long> interests;

    public static UserProfileResponse fromEntity(User user) {
        return UserProfileResponse.builder()
                .avatars(user.getCurrentAvatars())
                .nickname(user.getNickname())
                .age(user.getAge())
                .address(user.getAddress())
                .mbti(user.getMbti())
                .voice(user.getVoice())
                .interests(
                        user.getInterests().stream()
                                .map(UserInterest::getInterest)
                                .map(Interest::getId)
                                .collect(Collectors.toList()))
                .build();
    }
}
