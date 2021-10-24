package com.example.BlindCafe.exception;

import com.example.BlindCafe.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import static com.example.BlindCafe.exception.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.example.BlindCafe.exception.ErrorCode.INVALID_REQUEST;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    // 커스텀 에러
    @ExceptionHandler(BlindCafeException.class)
    public ErrorResponse handleException(
            BlindCafeException e, HttpServletRequest request
    ) {
        log.error("code : {}, url : {}, message : {}",
                e.getCode(), request.getRequestURI(), e.getMessage());

        return ErrorResponse.builder()
                .code(e.getCode())
                .message(e.getMessage())
                .build();
    }

    // 잘못 요청
    @ExceptionHandler(value = {
            HttpRequestMethodNotSupportedException.class,
            MethodArgumentNotValidException.class
    })
    public ErrorResponse handleBadRequest(
            BlindCafeException e, HttpServletRequest request
    ) {
        log.error("url : {}, message : {}",
                request.getRequestURI(), e.getMessage());

        return ErrorResponse.builder()
                .code(INVALID_REQUEST.getCode())
                .message(INVALID_REQUEST.getMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(
            Exception e, HttpServletRequest request
    ) {
        log.error("url : {}, message : {}",
                request.getRequestURI(), e.getMessage());

        return ErrorResponse.builder()
                .code(INTERNAL_SERVER_ERROR.getCode())
                .message(INTERNAL_SERVER_ERROR.getMessage())
                .build();
    }
}
