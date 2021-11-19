package com.example.BlindCafe.exception;

import com.example.BlindCafe.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import static com.example.BlindCafe.exception.CodeAndMessage.*;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    // 커스텀 에러
    @ExceptionHandler(BlindCafeException.class)
    public ResponseEntity<ErrorResponse> handleException(
            BlindCafeException e, HttpServletRequest request
    ) {
        log.error("code : {}, url : {}, message : {}",
                e.getCode(), request.getRequestURI(), e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getCode())
                .message(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // 잘못 요청
    @ExceptionHandler(value = {
            HttpRequestMethodNotSupportedException.class,
            MethodArgumentNotValidException.class,
            InvalidFormatException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            Exception e, HttpServletRequest request
    ) {
        log.error("url : {}, message : {}",
                request.getRequestURI(), e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(INVALID_REQUEST.getCode())
                .message(INVALID_REQUEST.getMessage())
                .build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e, HttpServletRequest request
    ) {
        log.error("url : {}, message : {}",
                request.getRequestURI(), e.getClass());
        e.printStackTrace();

        ErrorResponse errorResponse =  ErrorResponse.builder()
                .code(INTERNAL_SERVER_ERROR.getCode())
                .message(INTERNAL_SERVER_ERROR.getMessage())
                .build();
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
