package com.example.BlindCafe.exception;

import lombok.Getter;

@Getter
public class BlindCafeException extends RuntimeException {

    private String code;
    private String message;

    public BlindCafeException(CodeAndMessage errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
