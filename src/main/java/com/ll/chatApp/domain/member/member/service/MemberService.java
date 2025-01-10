package com.ll.chatApp.domain.member.member.service;

import com.ll.chatApp.domain.member.member.entity.Member;
import com.ll.chatApp.domain.member.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member join(String username, String password) {

        Member CheckedSignUpMember = memberRepository.findByUsername(username);

        if (CheckedSignUpMember != null) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다.");
        }

        //Encoder사용하여 패스워드 인코딩
        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();
        return memberRepository.save(member);
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }
}
