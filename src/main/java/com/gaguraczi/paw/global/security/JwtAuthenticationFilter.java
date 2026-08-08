package com.gaguraczi.paw.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticates requests carrying a valid access JWT and delegates processing to the filter chain.
     *
     * <p>Requests without a token, with an invalid token, or with a non-access token continue without
     * authentication. JWT processing errors result in an unauthorized response.</p>
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to continue
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = resolveToken(request);
            if (!StringUtils.hasText(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtTokenProvider.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // ACCESS 타입이 아닌 토큰(예: REFRESH)은 인증 없이 통과
            // → permitAll 엔드포인트는 정상 처리, 보호 엔드포인트는 Spring Security가 차단
            if (!JwtTokenProvider.TOKEN_TYPE_ACCESS.equals(jwtTokenProvider.parseTokenType(token))) {
                filterChain.doFilter(request, response);
                return;
            }

            String uid = jwtTokenProvider.parseUid(token);
            String role = jwtTokenProvider.parseRole(token);
            String authority = "ROLE_" + (StringUtils.hasText(role) ? role : "USER");

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    uid,
                    null,
                    List.of(new SimpleGrantedAuthority(authority))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.debug("JWT filter error: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts an access token from the Authorization header or the {@code accessToken} cookie.
     *
     * @param request the HTTP request containing the token
     * @return the access token, or {@code null} if no token is found
     */
    private static String resolveToken(HttpServletRequest request) {
        // Authorization 헤더 우선 (API 클라이언트 호환)
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        // HttpOnly 쿠키 폴백 (브라우저 OAuth2 로그인 이후 흐름)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
