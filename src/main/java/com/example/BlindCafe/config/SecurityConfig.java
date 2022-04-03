package com.example.BlindCafe.config;

import com.example.BlindCafe.config.jwt.JwtAuthorizationFilter;
import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.exception.ExceptionHandlerFilter;
import com.example.BlindCafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static String ADMIN_ID;
    public static String ADMIN_PW;

    @Value("${admin.id}")
    public void setAdminId(String value) { ADMIN_ID = value; }

    @Value("${admin.pw}")
    public void setAdminPw(String value) { ADMIN_PW = value; }

    private final UserRepository userRepository;
    private final ExceptionHandlerFilter exceptionHandlerFilter;

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
        // authorization
        http.authorizeRequests()
                .antMatchers("/", "/api/auth/login", "/api/auth/refresh").permitAll();
        // jwt filter
        http.addFilterBefore(
               new JwtAuthorizationFilter(userRepository),
               BasicAuthenticationFilter.class
        ).addFilterBefore(exceptionHandlerFilter, JwtAuthorizationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) {
        // 정적 리소스 spring security 대상에서 제외
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    public static Long getUid(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    public static boolean isAdmin(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.isAdmin();
    }
}
