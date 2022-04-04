package me.blindcafe.blindcafe.dto.response;

import me.blindcafe.blindcafe.domain.RetiredUser;
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
