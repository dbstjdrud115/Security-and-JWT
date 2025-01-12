package com.ll.chatApp.global.Security;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/*
* Spring Security의 User 클래스를 상속받아 사용중이며,
* 1. 생성자를 사용해서
*    아이디(id),유저명(username), 비번(password), 권한정보(authorities)로 유저정보 설정.
*    인증에 필요한 정보들이다.
*
* 2. Authentication auth에서 this.getPassword, getAuthority로 인증정보 취득
*    genAuthentication()는 JwtFilter에서 호출되는 부분이 있다.
*
* */
public class SecurityUser extends User {
    @Getter
    private long id;
    public SecurityUser(long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);//상위 클래스인 User의 기본생성자를 사용해서 유저정보 세팅
        this.id = id;
    }

    public Authentication genAuthentication() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                this,
                this.getPassword(),
                this.getAuthorities()
        );
        return auth;
    }
}
