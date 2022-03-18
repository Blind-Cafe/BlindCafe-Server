package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AvatarListResponse {
    List<String> avatars;

    public static AvatarListResponse fromEntity(User user) {
        return new AvatarListResponse(user.getCurrentAvatars());
    }
}
