package com.ll.chatApp.global.jwt;


import com.ll.chatApp.domain.member.member.entity.Member;
import com.ll.chatApp.global.util.Ut;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {

    //secret에서 계층구조에 맞게 설정한 jwt비밀키
    @Value("${custom.jwt.secretKey}")
    private String secretKeyOrigin;

    @Value("${custom.accessToken.expirationSeconds}")
    private int accessTokenExpirationSeconds;

    private SecretKey cachedSecretKey;

    //캐시키값이 없으면, secret에 설정한 비밀키를 Base64로 인코딩.
    /*
    * 1. secretKeyOrigin.getByte를 써서 바이트 배열로 변환
    *    ex : "MySecretKey" → [77, 121, 83, 101, 99, 114, 101, 116, 75, 101, 121]
    *
    * 2. 바이너리 데이터를 아스키 문자열인
    *    Base64로 인코딩(Base64.getEncoder().encodeToString())

    * 3. Base64로 인코딩된 문자열을 다시 바이트 배열로 변환(keyBase64Encoded.getBytes())
    * 4. HMAC-SHA 알고리즘을 사용하여 키를 만듬(Keys.hmacShaKeyFor())
    *
    * 5. genToken
    * */
    public SecretKey getSecretKey() {
        if (cachedSecretKey == null) {
            cachedSecretKey = cachedSecretKey = _getSecretKey();
        }
        return cachedSecretKey;
    }

    //이건 암호화는 아니고, 그냥 알고리즘과 인코딩을 여러번 섞어서 만든 고정값이다.
    private SecretKey _getSecretKey() {
        String keyBase64Encoded = Base64.getEncoder().encodeToString(secretKeyOrigin.getBytes());
        return Keys.hmacShaKeyFor(keyBase64Encoded.getBytes());
    }

    /*로그인할때 호출.*/
    public String genAccessToken(Member member) {
        return genToken(member, accessTokenExpirationSeconds);
    }

    public String genRefreshToken(Member member) {
        return genToken(member, 60 * 60 * 24 * 365 * 1);
    }

    public String genToken(Member member, int seconds) {
        Map<String, Object> claims = new HashMap<>();
        /*
        * 1. Jwts.builder() 호출시 헤더 자동생성
        * 2. 페이로드(.claim) 생성
        * 3. 서명(signWith) 생성.
        *
        * 아래 코드에선 생략되어있지만, 내부적으론
        * 헤더를 만들때 인코딩하고, 페이로드를 만들떄 인코딩하며,
        * 서명을 만들때 인코딩된 헤더와, 페이로드를 종합하여 서명을 만든다고 한다.
        *
        * 따라서 서명에 사용되는 비밀키값은 고정이지만, 페이로드가 가변적이라
        * 결과적으로 만들어지는 서명키값이 달라진다고 한다.
        * */
        claims.put("id", member.getId());
        claims.put("username", member.getUsername());
        long now = new Date().getTime();
        Date accessTokenExpiresIn = new Date(now + 1000L * seconds);
        return Jwts.builder()
                .claim("body", Ut.json.toStr(claims))
                .setExpiration(accessTokenExpiresIn)
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    //
    public Map getClaims(String token) {
        String body = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("body", String.class);
        return Ut.toMap(body);
    }

    // 유효성 검증
    public boolean verify (String token) {
        try {
            Jwts.parserBuilder()//jwt 파싱
                    .setSigningKey(getSecretKey())//yml에 설정하고, 인코딩한 키값을 서명키로 설정
                    .build()
                    .parseClaimsJws(token);//토큰과 SecretKey값 일치여부검증
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
