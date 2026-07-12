
package com.gaguraczi.paw.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.gaguraczi.paw.global.config.properties.JwtProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// 기존에 쓰던 JwtUtil과 동일한 기능 수행한다고 생각하면 편함.
@Component
public class JwtTokenProvider {

    public static final String CLAIM_TOKEN_TYPE = "typ";
    public static final String CLAIM_PROVIDER   = "provider";
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    private final SecretKey key;
    private final long accessExpMs;
    private final long refreshExpMs;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpMs = jwtProperties.getAccessExpMs();
        this.refreshExpMs = jwtProperties.getRefreshExpMs();
    }

    public String createAccessToken(String uid) {
        return buildToken(uid, null, TOKEN_TYPE_ACCESS, accessExpMs);
    }

    // provider: "naver", "local" 등 로그인 제공자 식별자
    public String createRefreshToken(String uid, String provider) {
        return buildToken(uid, provider, TOKEN_TYPE_REFRESH, refreshExpMs);
    }

    private String buildToken(String uid, String provider, String typ, long ttlMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMs);
        var builder = Jwts.builder()
                .subject(uid)
                .issuedAt(now)
                .expiration(exp)
                .claim(CLAIM_TOKEN_TYPE, typ);
        if (provider != null) {
            builder.claim(CLAIM_PROVIDER, provider);
        }
        return builder.signWith(key).compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | SecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String parseUid(String token) {
        return parseClaims(token).getSubject();
    }

    public String parseTokenType(String token) {
        Claims claims = parseClaims(token);
        Object typ = claims.get(CLAIM_TOKEN_TYPE);
        return typ != null ? typ.toString() : null;
    }

    public String parseProvider(String token) {
        Claims claims = parseClaims(token);
        Object provider = claims.get(CLAIM_PROVIDER);
        return provider != null ? provider.toString() : null;
    }

    public long getAccessExpMs() {
        return accessExpMs;
    }

    public long getRefreshExpMs() {
        return refreshExpMs;
    }
}
