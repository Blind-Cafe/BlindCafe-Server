package com.example.BlindCafe.config.jwt;


import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.UserRepository;
import com.example.BlindCafe.entity.type.status.UserStatus;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.example.BlindCafe.config.jwt.JwtProperties.HEADER_NAME;
import static com.example.BlindCafe.entity.type.status.UserStatus.SUSPENDED;
import static com.example.BlindCafe.exception.CodeAndMessage.*;

/**
 * JWT 필터
 */
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        String token = null;
        try {
            token = request.getHeader(HEADER_NAME);
        } catch (Exception ignored) {
        }

        if (token != null) {
            Authentication authentication = getUsernamePasswordAuthenticationToken(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    private Authentication getUsernamePasswordAuthenticationToken(String token) {
        try {
            String uid = JwtUtils.getUsedId(token);
            if (uid != null) {
                User user = userRepository.findById(Long.parseLong(uid))
                        .orElseThrow(() -> new BlindCafeException(FORBIDDEN_AUTHORIZATION));
                if (user.getStatus().equals(SUSPENDED))
                    throw new BlindCafeException(SUSPENDED_USER, user.getNickname());
                if (user.getStatus().equals(UserStatus.RETIRED))
                    throw new BlindCafeException(NO_USER);
                if (user != null) {
                    return new UsernamePasswordAuthenticationToken(user, null);
                }
            }
        } catch (ExpiredJwtException e) {
            throw new BlindCafeException(EXPIRED_TOKEN);
        } catch (IllegalArgumentException | MalformedJwtException e) {
            throw new BlindCafeException(FAILED_AUTHORIZATION);
        }
        return null;
    }
}
