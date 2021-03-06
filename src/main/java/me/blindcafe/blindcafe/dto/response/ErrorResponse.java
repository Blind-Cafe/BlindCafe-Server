package me.blindcafe.blindcafe.dto.response;

import lombok.*;

@Getter
@Setter
public class ErrorResponse extends ApiResponse {

    @Builder
    public ErrorResponse(String code, String message) {
        super(code, message);
    }
}
