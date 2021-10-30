package com.example.BlindCafe.config;

import com.example.BlindCafe.auth.jwt.JwtAccessDeniedHandler;
import com.example.BlindCafe.auth.jwt.JwtAuthenticationEntryPoint;
import com.example.BlindCafe.auth.jwt.JwtAuthorizationFilter;
import com.example.BlindCafe.exception.ExceptionHandlerFilter;
import com.example.BlindCafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserRepository userRepository;
    private final ExceptionHandlerFilter exceptionHandlerFilter;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // basic authentication
        http.httpBasic().disable(); // basic authentication filter 비활성화
        // csrf
        http.csrf().disable();
        // remember-me
        http.rememberMe().disable();
        // stateless
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // jwt filter
        http.addFilterBefore(
                new JwtAuthorizationFilter(userRepository),
                BasicAuthenticationFilter.class
        ).addFilterBefore(exceptionHandlerFilter, JwtAuthorizationFilter.class);;
        // authorization
        http.authorizeRequests()
                .antMatchers("/", "/api/kakao", "/api/apple").permitAll();
    }

    @Override
    public void configure(WebSecurity web) {
        // 정적 리소스 spring security 대상에서 제외
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
