package me.blindcafe.blindcafe.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private Long uid;
    private String nickname;
    private String accessToken;
    private String refreshToken;

    public LoginResponse(Long uid, String accessToken, String refreshToken) {
        this.uid = uid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
