package com.ll.chatApp.domain.member.member.controller;

import com.ll.chatApp.domain.member.member.dto.MemberDto;
import com.ll.chatApp.domain.member.member.dto.MemberRequest;
import com.ll.chatApp.domain.member.member.entity.Member;
import com.ll.chatApp.domain.member.member.service.MemberService;
import com.ll.chatApp.global.jwt.JwtProvider;
import com.ll.chatApp.global.rsData.RsData;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public RsData<MemberDto> signup(@Valid @RequestBody MemberRequest memberRequest) {
        //Security 및 Encoder 사용.
        Member member = memberService.join(memberRequest.getUsername(), memberRequest.getPassword());
        return new RsData<>("200", "회원가입에 성공하였습니다.", new MemberDto(member));
    }

    @PostMapping("/login")
    public RsData<Void> login(@Valid @RequestBody MemberRequest memberRequest,
                                HttpServletResponse res) {

        Member member = memberService.getMember(memberRequest.getUsername());

        //토큰생성
        String token = jwtProvider.genAccessToken(member);

        //보안설정 추가. 이 내용을 설정하면 개발자도구등에서
        //수정할 수 없다.
        Cookie cookie = new Cookie("accessToken", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(60*60);

        //회신할 쿠키 생성
        res.addCookie(cookie);


        String refreshToken = member.getRefreshToken();
            Cookie refreshTokenCookie  = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(60 * 60);
        res.addCookie(refreshTokenCookie);



        return new RsData<>("200", "로그인~");
    }

    @GetMapping("/logout")
    public RsData<Void> logout(HttpServletResponse response) {

        // 로그아웃시 토큰을 빙깡통으로 만들어, 쿠키에 담아 전송
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);


        Cookie refreshTokenCookie  = new Cookie("refreshToken", null);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);


        return new RsData<>("200", "로그아웃에 성공하였습니다.");
    }

    @GetMapping("/me")
    public RsData<Void> me(HttpServletRequest req) {

        /*
        * request에 담겨온 쿠키배열속에서,
        * accessToken 이란 항목을 깐다.
        *
        * accessToken을 복호화하여
        * 그 속의 username내용물을 깐다.
        * */

        Cookie[] cookies = req.getCookies();
        String accessToken = "";

        for(Cookie cookie : cookies){
            if(cookie.getName().equals("accessToken")){
                accessToken = cookie.getValue();
            }
        }

        Map<String,Object> claims = jwtProvider.getClaims(accessToken);
        String username = (String)claims.get("username");
        this.memberService.getMember(username);
        return new RsData<>("200", "방가", null);
    }
}
