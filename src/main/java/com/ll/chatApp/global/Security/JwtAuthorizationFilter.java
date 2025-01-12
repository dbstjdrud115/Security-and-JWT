package com.ll.chatApp.global.Security;


import com.ll.chatApp.domain.member.member.service.MemberService;
import com.ll.chatApp.global.rsData.RsData;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final MemberService memberService;

    /*ApiSecurity에서 인증 필터로 걸어둔 JwtAuthorizationFilter를 호출할떄,
        OncePerRequestFilter.doFilter가 실행되며 해당 메서드 내부에서
                            doFilterInternal를 호출한다.

        따라서 우리가 재정의한 doFilterInternal를 호출하게 된다.


        filterChain.doFilter는 FilterChain을 유지하고,
                              다음 필터로 요청을 전달하는데,
                              우리 코드에선
                              SecurityFilterChain apiFilterChain(HttpSecurity http)와 그 내부에서 호출하는
                              jwtAuthorizationFilter
                              UsernamePasswordAuthenticationFilter
                              같은 필터들이 있고, filterChain은 이를 관리한다.

                       Spring Security는 필터를 자동으로 관리하는지라
                       doFilter가 넘겨줄 다음 Step이 무엇인지는 신경쓸 필요가 없고,
                       그냥 걸고싶은 필터만 잘 개발하면 되는듯.
     */
    @Override
    @SneakyThrows
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        if (request.getRequestURI().equals("/api/v1/members/login") || request.getRequestURI().equals("/api/v1/members/logout")) {
            filterChain.doFilter(request, response);
            return;
        }


        /*
         * req.cookie에 String accessToken값이 있는 경우에,
         *
         * 1. 유효성을 검사한다
         *     validateToken은 return jwtProvider.verify(token)인데,
         *     쿠키의 accessToken값과 JwtProvider내부에서 암호화한 SecretKey를
         *      대조하여 헤더.페이로드.서명 중에 서명을 검증한다.
         *
         * 2. 유효성 검증 후, refreshToken값을 가져와서
         *      DB에 일치하는 refreshToken이 있는지 찔러보고 있으면 genAccessToken으로
         *      토큰을 만들어준다.
         * */

        //findByRefreshToken에서 DB를 어떻게 찌르는지 과정도 한 번 봐야할듯.

        String accessToken = _getCookie("accessToken");

        if (!accessToken.isBlank()) {
            if (!memberService.validateToken(accessToken)) {
                String refreshToken = _getCookie("refreshToken");
                RsData<String> rs = memberService.refreshAccessToken(refreshToken);
                _addHeaderCookie("accessToken", rs.getData());
            }
            // securityUser 가져오기
            SecurityUser securityUser = memberService.getUserFromAccessToken(accessToken);
            // 인가 처리
            SecurityContextHolder.getContext().setAuthentication(securityUser.genAuthentication());
        }
        filterChain.doFilter(request, response);
    }

    /*
    * req.cookie를 까서 accessToken과 이름이 일치하는 값이 있는지 확인하고
    * 일치하는 대상들의 값을 추출한다.
    * */
    private String _getCookie(String name) {
        Cookie[] cookies = req.getCookies();
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst()
                .map(Cookie::getValue)
                .orElse("");
    }
    private void _addHeaderCookie(String tokenName, String token) {
        ResponseCookie cookie = ResponseCookie.from(tokenName, token)
                .path("/")
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .build();
        resp.addHeader("Set-Cookie", cookie.toString());
    }
}