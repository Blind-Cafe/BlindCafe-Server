package me.blindcafe.blindcafe.config.jwt;

import me.blindcafe.blindcafe.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static me.blindcafe.blindcafe.exception.CodeAndMessage.FORBIDDEN_AUTHORIZATION;

@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        log.error("code : 4003, url : {}, message : {}",
                request.getRequestURI(), accessDeniedException.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(FORBIDDEN_AUTHORIZATION.getCode())
                .message(FORBIDDEN_AUTHORIZATION.getMessage())
                .build();

        byte[] responseToSend = restResponseBytes(errorResponse);
        response.setHeader("Content-Type", "application/json");
        response.setStatus(403);
        response.getOutputStream().write(responseToSend);
    }

    private byte[] restResponseBytes(ErrorResponse eErrorResponse) throws IOException {
        String serialized = new ObjectMapper().writeValueAsString(eErrorResponse);
        return serialized.getBytes();
    }
}
