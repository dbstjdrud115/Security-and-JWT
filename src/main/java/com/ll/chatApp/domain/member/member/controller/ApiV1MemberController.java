package com.ll.chatApp.domain.member.member.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class ApiV1MemberController {

    @PostMapping("/signup")
    public void signup(){
        System.out.println("가입");
    }

    @PostMapping("/login")
    public void login(){
        System.out.println("로그인");
    }

    @PostMapping("/logout")
    public void logout(){
        System.out.println("로그아웃");
    }

    @PostMapping("/me")
    public void me(){
        System.out.println("나");
    }
}
