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

    @Value(("${custom.accessToken.expirationSeconds}"))
    private int accessTokenExpirationSeconds;

    private SecretKey cachedSecretKey;

    //캐시키값이 없으면, secret에 설정한 비밀키를 Base64로 인코딩.
    /*
    * 1. secretKeyOrigin.getByte를 써서 바이트 배열로 변환
    *
    *    ex : "MySecretKey" → [77, 121, 83, 101, 99, 114, 101, 116, 75, 101, 121]
    *
    * 2. Base64로 인코딩(Base64.getEncoder().encodeToString())
    * 3. Base64로 인코딩된 문자열을 다시 바이트 배열로 변환(keyBase64Encoded.getBytes())
    * 4. HMAC-SHA 알고리즘을 사용하여 키를 만듬(Keys.hmacShaKeyFor())
    *
    * */
    public SecretKey getSecretKey() {
        if (cachedSecretKey == null) {
            cachedSecretKey = cachedSecretKey = _getSecretKey();
        }
        return cachedSecretKey;
    }

    private SecretKey _getSecretKey() {
        String keyBase64Encoded = Base64.getEncoder().encodeToString(secretKeyOrigin.getBytes());
        return Keys.hmacShaKeyFor(keyBase64Encoded.getBytes());
    }

    public String genAccessToken(Member member, int seconds){
        return genToken(member, accessTokenExpirationSeconds);
    }

    public String genRefreshToken(Member member){
        return genToken(member, 60*10*10);
    }

    public String genToken(Member member, int seconds) {
        Map<String, Object> claims = new HashMap<>();

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
}
