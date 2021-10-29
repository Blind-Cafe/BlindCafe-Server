package com.example.BlindCafe.auth.jwt;

import com.example.BlindCafe.dto.ErrorResponse;
import com.example.BlindCafe.exception.BlindCafeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.example.BlindCafe.exception.CodeAndMessage.NON_AUTHORIZATION;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        log.error("code : 4001, url : {}, message : {}",
                request.getRequestURI(), authException.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(NON_AUTHORIZATION.getCode())
                .message(NON_AUTHORIZATION.getMessage())
                .build();

        byte[] responseToSend = restResponseBytes(errorResponse);
        ((HttpServletResponse) response).setHeader("Content-Type", "application/json");
        ((HttpServletResponse) response).setStatus(401);
        response.getOutputStream().write(responseToSend);
    }

    private byte[] restResponseBytes(ErrorResponse eErrorResponse) throws IOException {
        String serialized = new ObjectMapper().writeValueAsString(eErrorResponse);
        return serialized.getBytes();
    }
}