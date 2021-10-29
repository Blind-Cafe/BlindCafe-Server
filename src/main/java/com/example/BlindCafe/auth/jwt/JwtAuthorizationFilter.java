package com.example.BlindCafe.auth.jwt;


import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
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

import static com.example.BlindCafe.auth.jwt.JwtProperties.HEADER_NAME;
import static com.example.BlindCafe.exception.CodeAndMessage.*;

/**
 * JWT 필터
 */
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
        } catch (Exception e) {
            throw new BlindCafeException(NON_AUTHORIZATION);
        }
        if (token != null) {
            String socialId = null;
            try {
                socialId = JwtUtils.getUserSocialId(token);
            } catch (ExpiredJwtException e) {
                throw new BlindCafeException(EXPIRED_TOKEN);
            } catch (IllegalArgumentException e) {
                throw new BlindCafeException(FORBIDDEN_AUTHORIZATION);
            }

            if (socialId != null) {
                User user = userRepository.findBySocialId(socialId)
                        .orElseThrow(() -> new BlindCafeException(NON_AUTHORIZATION));

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(user, null);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                throw new BlindCafeException(NON_AUTHORIZATION);
            }
        }
        chain.doFilter(request, response);
    }
}
