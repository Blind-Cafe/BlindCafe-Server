package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.RetiredUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteUserResponse {
    private String nickname;

    public static DeleteUserResponse fromEntity(RetiredUser retiredUser) {
        return new DeleteUserResponse(retiredUser.getNickname());
    }
}
