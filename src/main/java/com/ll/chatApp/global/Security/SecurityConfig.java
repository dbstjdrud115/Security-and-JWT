package com.ll.chatApp.global.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /*
    * ApiSecurityConfig와 SecurityConfig로 이중화되어있다.
    * apisecurity는 특별히 api가 포함된 req에 대한 보안처리를 하지만,
    * 그외 요구에 대해선 여기 SecurityConfig에서 처리한다..
    *
    * 이런 이중화의 예시로 은행권의 경우
    * 일반 웹 요청(예: 공지사항 조회)은 기본적인 세션 인증을 사용하고,
    * 중요한 API 요청(예: 계좌 이체)은 JWT와 다단계 인증(MFA)등을 조합한다고 한다.
    *
    * */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
        ;
        return http.build();
    }
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}