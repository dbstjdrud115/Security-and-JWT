package com.ll.chatApp.domain.member.member.controller;

import com.ll.chatApp.domain.member.member.dto.MemberDto;
import com.ll.chatApp.domain.member.member.dto.MemberRequest;
import com.ll.chatApp.domain.member.member.entity.Member;
import com.ll.chatApp.domain.member.member.repository.MemberRepository;
import com.ll.chatApp.domain.member.member.service.MemberService;
import com.ll.chatApp.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class ApiV1MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping("/signup")
    public RsData<MemberDto> signup(@Valid @RequestBody MemberRequest memberRequest) {
        //Security 및 Encoder 사용.
        Member member = memberService.join(memberRequest.getUsername(), memberRequest.getPassword());
        return new RsData<>("200", "회원가입에 성공하였습니다.", new MemberDto(member));
    }

    @PostMapping("/login")
    public void login() {
        System.out.println("login");
    }

    @GetMapping("/logout")
    public void logout() {
        System.out.println("logout");
    }

    @GetMapping("/me")
    public void me() {
        System.out.println("me");
    }
}
