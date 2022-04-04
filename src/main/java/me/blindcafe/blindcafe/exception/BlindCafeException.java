package me.blindcafe.blindcafe.exception;

import lombok.Getter;

@Getter
public class BlindCafeException extends RuntimeException {

    private final String code;
    private final String message;
    private String nickname;

    public BlindCafeException(CodeAndMessage errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public BlindCafeException(CodeAndMessage errorCode, String nickname) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.nickname = nickname;
    }
}
