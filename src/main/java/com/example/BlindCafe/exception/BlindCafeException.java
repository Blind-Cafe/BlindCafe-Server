package com.example.BlindCafe.exception;

import lombok.Getter;

@Getter
public class BlindCafeException extends RuntimeException {

    private String code;
    private String message;
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
