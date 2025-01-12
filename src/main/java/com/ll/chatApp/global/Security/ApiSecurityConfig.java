package com.ll.chatApp.global.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ApiSecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    /*
    * SecurityFilterChain = 특정 Request에 대해 어떤 보안 규칙을 적용할지 설정.
    *                       매치되는 api(requestMatchers)는 전체 허용하되(permitAll)
    *                       그외에는 전부 인증필요(anyRequest().authenticated())
    *
    *
    * csrf = csrf(Cross-Site Request Forgery) 보호 비활성화
    *        예를들어, 대검찰청 피싱사이트를 피해자가 접속하면, 사용자가 입력한 정보를 탈취해서
    *         원래 대검찰청 사이트에 들어가 해킹을 시도하는것.
    *
    *         근데 여기서 우리는 jwt를 쓰다보니, 토큰없으면 말짱황이라서 보호 비활성화 한듯.
    *
    * httpBasic =  인증방식의 하나. 클라가 request시
    *              사용자정보를 base64로 인코딩해서 전송.
    *              근데 디코딩이 쉽고, 인증정보를 서버에서 검증하는 부담이 있다.
    *
    * formLogin = 폼 로그인
    * sessionManagement~~ = 세션에 대한 무상태성(STATELESS)유지.
    *
    * 결론부터 말해, JWTFilter를 쓰기에, http.csrf,http.httpBasic같은 설정들이 아래처럼
    * 세팅된것이다.
    * */

    @Bean
    SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/**")//req에 api포함시 설정 적용
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(HttpMethod.GET, "/api/*/articles").permitAll()//get요청이며 artice포함시 전체 허용
                        .requestMatchers(HttpMethod.GET, "/api/*/articles/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/*/members/login").permitAll() // 로그인은 누구나 가능, post 요청만 허용
                        .requestMatchers(HttpMethod.GET, "/api/*/members/logout").permitAll()
                        .anyRequest().authenticated()//permit외에 인증요구.
                )
                .csrf(csrf -> csrf.disable()) // csrf 토큰 끄기
                .httpBasic(httpBasic -> httpBasic.disable()) // httpBasic 로그인 방식 끄기
                .formLogin(formLogin -> formLogin.disable()) // 폼 로그인 방식 끄기
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(
                        jwtAuthorizationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );//AuthorizationFilter는 .anyRequest().authenticated()의 인증요구를 처리한다.
                  //그러니까 현재 코드에선 permitAll()외의 request에 대해 jwt기반 인증요구를 수행한다는것이다.
        return http.build();
    }
}