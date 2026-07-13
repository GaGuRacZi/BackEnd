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

    /**
     * Creates a token provider using the configured signing secret and token expiration periods.
     *
     * @param jwtProperties the JWT configuration containing the signing secret and expiration periods
     */
    public JwtTokenProvider(JwtProperties jwtProperties) {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpMs = jwtProperties.getAccessExpMs();
        this.refreshExpMs = jwtProperties.getRefreshExpMs();
    }

    /**
     * Creates an access token for the specified user.
     *
     * @param uid the user identifier
     * @return a signed access token
     */
    public String createAccessToken(String uid) {
        return buildToken(uid, null, TOKEN_TYPE_ACCESS, accessExpMs);
    }

    /**
     * Creates a refresh token for the specified user and login provider.
     *
     * @param uid      the user identifier
     * @param provider the login provider identifier
     * @return the generated refresh token
     */
    public String createRefreshToken(String uid, String provider) {
        return buildToken(uid, provider, TOKEN_TYPE_REFRESH, refreshExpMs);
    }

    /**
     * Builds a signed JWT with the specified subject, token type, provider, and lifetime.
     *
     * @param uid      the subject identifier
     * @param provider  the authentication provider, or {@code null} if unavailable
     * @param typ      the token type
     * @param ttlMs    the token lifetime in milliseconds
     * @return the compact signed JWT
     */
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

    /**
     * Determines whether a JWT is valid and can be parsed.
     *
     * @param token the JWT to validate
     * @return      {@code true} if the token is valid, {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | SecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses and verifies a signed JWT.
     *
     * @param token the signed JWT to parse
     * @return the claims contained in the token
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the user identifier from a JWT.
     *
     * @param token the JWT to parse
     * @return the user identifier stored as the token subject
     */
    public String parseUid(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the token type from a JWT.
     *
     * @param token the JWT to inspect
     * @return the token type, or {@code null} if the claim is absent
     */
    public String parseTokenType(String token) {
        Claims claims = parseClaims(token);
        Object typ = claims.get(CLAIM_TOKEN_TYPE);
        return typ != null ? typ.toString() : null;
    }

    /**
     * Extracts the login provider from a JWT.
     *
     * @param token the JWT containing the provider claim
     * @return the provider value, or {@code null} if the claim is absent
     */
    public String parseProvider(String token) {
        Claims claims = parseClaims(token);
        Object provider = claims.get(CLAIM_PROVIDER);
        return provider != null ? provider.toString() : null;
    }

    /**
     * Retrieves the configured access-token expiration duration.
     *
     * @return the access-token expiration duration in milliseconds
     */
    public long getAccessExpMs() {
        return accessExpMs;
    }

    /**
     * Retrieves the configured refresh token expiration duration.
     *
     * @return the refresh token expiration duration in milliseconds
     */
    public long getRefreshExpMs() {
        return refreshExpMs;
    }
}
